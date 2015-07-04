package eu.over9000.veya.gui;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;

public class Gui {

	/** The string that is rendered on-screen. */
	private static final StringBuilder renderString = new StringBuilder("Enter your text");
	public static final int ASCII_OFFSET = 32;
	public static final int GRID_SIZE = 16;
	public static final float CELL_SIZE = 1.0f / GRID_SIZE;
	/** The texture object for the bitmap font. */
	private static int fontTexture;

	public static void main(final String[] args) {
		setUpDisplay();
		try {
			setUpTextures();
		} catch (final IOException e) {
			e.printStackTrace();
			cleanUp(true);
		}
		setUpStates();
		enterGameLoop();
		cleanUp(false);
	}

	private static void setUpDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(1280, 720));
			Display.setTitle("Bitmap Fonts");
			Display.create();
		} catch (final LWJGLException e) {
			e.printStackTrace();
			cleanUp(true);
		}
	}

	private static void setUpTextures() throws IOException {
		// Create a new texture for the bitmap font.
		fontTexture = GL11.glGenTextures();
		// Bind the texture object to theGL11.GL_TEXTURE_2D target, specifying that it will be a 2D texture.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTexture);
		// Use TWL's utility classes to load the png file.
		final PNGDecoder decoder = new PNGDecoder(Gui.class.getResourceAsStream("/textures/font.png"));
		final ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
		buffer.flip();
		// Load the previously loaded texture data into the texture object.
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		// Unbind the texture.
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	private static void setUpStates() {
		GL11.glClearColor(0, 0.3f, 0, 1);
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
		renderString(renderString.toString(), fontTexture, -0.9f, 0, 0.075f);
	}

	/**
	 * Renders text using a font bitmap.
	 *
	 * @param string
	 * 		the string to render
	 * @param textureObject
	 * 		the texture object containing the fontGL11.glyphs
	 * @param x
	 * 		the x-coordinate of the bottom-left corner of where the string starts rendering
	 * @param y
	 * 		the y-coordinate of the bottom-left corner of where the string starts rendering
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
			final float characterHeight = FontMetrics.getCharHeight(string.charAt(i));

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