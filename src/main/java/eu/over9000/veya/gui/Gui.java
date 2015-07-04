package eu.over9000.veya.gui;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Gui {

    /**
     * The string that is rendered on-screen.
     */
    private static final StringBuilder renderString = new StringBuilder("Enter your text");
    public static final int ASCII_OFFSET = 32;
    public static final int GRID_SIZE = 16;
    public static final float CELL_SIZE = 1.0f / GRID_SIZE;
    /**
     * The texture object for the bitmap font.
     */
    private static int fontTexture;
    private static TrueTypeFont font;

    public static void main(final String[] args) {
        setUpStates(1280, 720);

        setUpFonts();

        enterGameLoop();
        cleanUp(false);
    }

    private static void setUpFonts() {
        font = new TrueTypeFont(new Font("Arial", Font.PLAIN, 24), true, new char[]{'€'});
    }

    private static void setUpStates(int width, int height) {
        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create();
            Display.setVSyncEnabled(true);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glClearColor(0.0f, 0.3f, 0.0f, 0.0f);
        GL11.glClearDepth(1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private static void enterGameLoop() {
        while (!Display.isCloseRequested()) {
            render();
            input();
            update();
        }
    }

    private static void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        //renderString(renderString.toString(), fontTexture, -0.9f, 0, 0.075f);
        font.drawString(200, 100, "WASD QqQ getting @€µ", Color.WHITE);
    }

    /**
     * Renders text using a font bitmap.
     *
     * @param string        the string to render
     * @param textureObject the texture object containing the fontGL11.glyphs
     * @param x             the x-coordinate of the bottom-left corner of where the string starts rendering
     * @param y             the y-coordinate of the bottom-left corner of where the string starts rendering
     */
    private static void renderString(final String string, final int textureObject, final float x, final float y, final float scaling) {
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureObject);

        // Enable additive blending. This means that the colours will be added to already existing colours in the
        // frame buffer. In practice, this makes the black parts of the texture become invisible.
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        // Store the current model-view matrix.
        GL11.glPushMatrix();
        // Offset all subsequent (at least up until 'glPopMatrix') vertex coordinates.
        GL11.glTranslatef(x, y, 0);
        GL11.glBegin(GL11.GL_QUADS);
        // Iterate over all the characters in the string.

        float currOffset = 0;

        for (int i = 0; i < string.length(); i++) {
            final int asciiCode = string.charAt(i) - ASCII_OFFSET;

            final float characterWidth = FontMetrics.getCharWidth(string.charAt(i));
            final float characterHeight = FontMetrics.getCharHeight();

            final float characterWidthScaled = characterWidth * scaling;
            final float characterHeightScaled = characterHeight * scaling;

            final float characterWidthCellScaled = characterWidth * CELL_SIZE;
            final float characterHeightCellScaled = characterHeight * CELL_SIZE;


            final float cellX = (asciiCode % GRID_SIZE) * CELL_SIZE;
            final float cellY = (asciiCode / GRID_SIZE) * CELL_SIZE;


            GL11.glTexCoord2f(cellX, cellY + characterHeightCellScaled);
            GL11.glVertex2f(currOffset, y);
            GL11.glTexCoord2f(cellX + characterWidthCellScaled, cellY + characterHeightCellScaled);
            GL11.glVertex2f(currOffset + characterWidthScaled, y);
            GL11.glTexCoord2f(cellX + characterWidthCellScaled, cellY);
            GL11.glVertex2f(currOffset + characterWidthScaled, y + characterHeightScaled);
            GL11.glTexCoord2f(cellX, cellY);
            GL11.glVertex2f(currOffset, y + characterHeightScaled);

            currOffset += characterWidthScaled;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void input() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                // Reset the string if we press escape.
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    renderString.setLength(0);
                }
                // Append the pressed key to the string if the key isn't the back key or the shift key.
                if (Keyboard.getEventKey() != Keyboard.KEY_BACK) {
                    if (Keyboard.getEventKey() != Keyboard.KEY_LSHIFT) {
                        renderString.append(Keyboard.getEventCharacter());
                        //                        renderString.append((char) Keyboard.getEventCharacter() - 1);
                    }
                    // If the key is the back key, shorten the string by one character.
                } else if (renderString.length() > 0) {
                    renderString.setLength(renderString.length() - 1);
                }
            }
        }
    }

    private static void update() {
        Display.update();
        Display.sync(60);
    }

    private static void cleanUp(final boolean asCrash) {
        GL11.glDeleteTextures(fontTexture);
        Display.destroy();
        System.exit(asCrash ? 1 : 0);
    }
}