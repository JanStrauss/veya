package eu.over9000.veya.gui;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * A TrueType font implementation for Slick
 *
 * @author James Chambers (Jimmy)
 * @author Jeremy Adams (elias4444)
 * @author Kevin Glass (kevglass)
 * @author Peter Korzuszek (genail)
 */
public class TrueTypeFont {

    /**
     * Array that holds necessary information about the font characters
     */
    private IntObject[] charArray = new IntObject[256];

    /**
     * Map of user defined font characters (Character <-> IntObject)
     */
    private Map customChars = new HashMap();

    /**
     * Boolean flag on whether AntiAliasing is enabled or not
     */
    private boolean antiAlias;

    /**
     * Font's size
     */
    private int fontSize = 0;

    /**
     * Font's height
     */
    private int fontHeight = 0;


    private int textureId;

    /**
     * Default font texture width
     */
    private int textureWidth = 512;

    /**
     * Default font texture height
     */
    private int textureHeight = 512;

    /**
     * A reference to Java's AWT Font that we create our font texture from
     */
    private java.awt.Font font;

    /**
     * The font metrics for our Java AWT font
     */
    private FontMetrics fontMetrics;


    /**
     * This is a special internal class that holds our necessary information for
     * the font characters. This includes width, height, and where the character
     * is stored on the font texture.
     */
    private class IntObject {
        /**
         * Character's width
         */
        public int width;

        /**
         * Character's height
         */
        public int height;

        /**
         * Character's stored x position
         */
        public int storedX;

        /**
         * Character's stored y position
         */
        public int storedY;
    }

    /**
     * Constructor for the TrueTypeFont class Pass in the preloaded standard
     * Java TrueType font, and whether you want it to be cached with
     * AntiAliasing applied.
     *
     * @param font            Standard Java AWT font
     * @param antiAlias       Whether or not to apply AntiAliasing to the cached font
     * @param additionalChars Characters of font that will be used in addition of first 256 (by unicode).
     */
    public TrueTypeFont(java.awt.Font font, boolean antiAlias, char[] additionalChars) {

        this.font = font;
        this.fontSize = font.getSize();
        this.antiAlias = antiAlias;

        createSet(additionalChars);
    }

    /**
     * Constructor for the TrueTypeFont class Pass in the preloaded standard
     * Java TrueType font, and whether you want it to be cached with
     * AntiAliasing applied.
     *
     * @param font      Standard Java AWT font
     * @param antiAlias Whether or not to apply AntiAliasing to the cached font
     */
    public TrueTypeFont(java.awt.Font font, boolean antiAlias) {
        this(font, antiAlias, null);
    }

    /**
     * Create a standard Java2D BufferedImage of the given character
     *
     * @param ch The character to create a BufferedImage for
     * @return A BufferedImage containing the character
     */
    private BufferedImage getFontImage(char ch) {
        // Create a temporary image to extract the character's size
        BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) tempfontImage.getGraphics();
        if (antiAlias == true) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        fontMetrics = g.getFontMetrics();
        int charwidth = fontMetrics.charWidth(ch);

        if (charwidth <= 0) {
            charwidth = 1;
        }
        int charheight = fontMetrics.getHeight();
        if (charheight <= 0) {
            charheight = fontSize;
        }

        // Create another image holding the character we are creating
        BufferedImage fontImage;
        fontImage = new BufferedImage(charwidth, charheight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gt = (Graphics2D) fontImage.getGraphics();
        if (antiAlias == true) {
            gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        gt.setFont(font);

        gt.setColor(Color.WHITE);
        int charx = 0;
        int chary = 0;
        gt.drawString(String.valueOf(ch), (charx), (chary) + fontMetrics.getAscent());

        return fontImage;

    }

    /**
     * Create and store the font
     *
     * @param customCharsArray Characters that should be also added to the cache.
     */
    private void createSet(char[] customCharsArray) {
        // If there are custom chars then I expand the font texture twice
        if (customCharsArray != null && customCharsArray.length > 0) {
            textureWidth *= 2;
        }

        // In any case this should be done in other way. Texture with size 512x512
        // can maintain only 256 characters with resolution of 32x32. The texture
        // size should be calculated dynamicaly by looking at character sizes.

        try {

            BufferedImage imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) imgTemp.getGraphics();

            g.setColor(new Color(255, 255, 255, 1));
            g.fillRect(0, 0, textureWidth, textureHeight);

            int rowHeight = 0;
            int positionX = 0;
            int positionY = 0;

            int customCharsLength = (customCharsArray != null) ? customCharsArray.length : 0;

            for (int i = 0; i < 256 + customCharsLength; i++) {

                // get 0-255 characters and then custom characters
                char ch = (i < 256) ? (char) i : customCharsArray[i - 256];

                BufferedImage fontImage = getFontImage(ch);

                IntObject newIntObject = new IntObject();

                newIntObject.width = fontImage.getWidth();
                newIntObject.height = fontImage.getHeight();

                if (positionX + newIntObject.width >= textureWidth) {
                    positionX = 0;
                    positionY += rowHeight;
                    rowHeight = 0;
                }

                newIntObject.storedX = positionX;
                newIntObject.storedY = positionY;

                if (newIntObject.height > fontHeight) {
                    fontHeight = newIntObject.height;
                }

                if (newIntObject.height > rowHeight) {
                    rowHeight = newIntObject.height;
                }

                // Draw it here
                g.drawImage(fontImage, positionX, positionY, null);

                positionX += newIntObject.width;

                if (i < 256) { // standard characters
                    charArray[i] = newIntObject;
                } else { // custom characters
                    customChars.put(new Character(ch), newIntObject);
                }

                fontImage = null;
            }

            setUpTextures(imgTemp);

        } catch (IOException e) {
            System.err.println("Failed to create font.");
            e.printStackTrace();
        }
    }

    private void setUpTextures(BufferedImage imgTemp) throws IOException {
        // Create a new texture for the bitmap font.
        textureId = GL11.glGenTextures();
        // Bind the texture object to theGL11.GL_TEXTURE_2D target, specifying that it will be a 2D texture.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        // Use TWL's utility classes to load the png file.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(imgTemp, "png", byteArrayOutputStream);
        ImageIO.write(imgTemp, "png", new File("kek.png"));

        final PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        final ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buffer.flip();
        // Load the previously loaded texture data into the texture object.
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        // Unbind the texture.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }


    /**
     * Draw a textured quad
     *
     * @param drawX  The left x position to draw to
     * @param drawY  The top y position to draw to
     * @param drawX2 The right x position to draw to
     * @param drawY2 The bottom y position to draw to
     * @param srcX   The left source x position to draw from
     * @param srcY   The top source y position to draw from
     * @param srcX2  The right source x position to draw from
     * @param srcY2  The bottom source y position to draw from
     */
    private void drawQuad(float drawX, float drawY, float drawX2, float drawY2, float srcX, float srcY, float srcX2, float srcY2) {
        float DrawWidth = drawX2 - drawX;
        float DrawHeight = drawY2 - drawY;
        float TextureSrcX = srcX / textureWidth;
        float TextureSrcY = srcY / textureHeight;
        float SrcWidth = srcX2 - srcX;
        float SrcHeight = srcY2 - srcY;
        float RenderWidth = (SrcWidth / textureWidth);
        float RenderHeight = (SrcHeight / textureHeight);

        GL11.glTexCoord2f(TextureSrcX, TextureSrcY);
        GL11.glVertex2f(drawX, drawY);
        GL11.glTexCoord2f(TextureSrcX, TextureSrcY + RenderHeight);
        GL11.glVertex2f(drawX, drawY + DrawHeight);
        GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY + RenderHeight);
        GL11.glVertex2f(drawX + DrawWidth, drawY + DrawHeight);
        GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY);
        GL11.glVertex2f(drawX + DrawWidth, drawY);
    }

    /**
     * Get the width of a given String
     *
     * @param whatchars The characters to get the width of
     * @return The width of the characters
     */
    public int getWidth(String whatchars) {
        int totalwidth = 0;
        IntObject intObject = null;
        int currentChar = 0;
        for (int i = 0; i < whatchars.length(); i++) {
            currentChar = whatchars.charAt(i);
            if (currentChar < 256) {
                intObject = charArray[currentChar];
            } else {
                intObject = (IntObject) customChars.get(new Character((char) currentChar));
            }

            if (intObject != null)
                totalwidth += intObject.width;
        }
        return totalwidth;
    }

    /**
     * Get the font's height
     *
     * @return The height of the font
     */
    public int getHeight() {
        return fontHeight;
    }

    /**
     * Get the height of a String
     *
     * @return The height of a given string
     */
    public int getHeight(String HeightString) {
        return fontHeight;
    }

    /**
     * Get the font's line height
     *
     * @return The line height of the font
     */
    public int getLineHeight() {
        return fontHeight;
    }


    public void drawString(float x, float y, String whatchars, Color color) {
        drawString(x, y, whatchars, color, 0, whatchars.length() - 1);
    }


    public void drawString(float x, float y, String whatchars, Color color, int startIndex, int endIndex) {
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        IntObject intObject = null;
        int charCurrent;

        GL11.glBegin(GL11.GL_QUADS);

        int totalwidth = 0;
        for (int i = 0; i < whatchars.length(); i++) {
            charCurrent = whatchars.charAt(i);
            if (charCurrent < 256) {
                intObject = charArray[charCurrent];
            } else {
                intObject = (IntObject) customChars.get(new Character((char) charCurrent));
            }

            if (intObject != null) {
                if ((i >= startIndex) || (i <= endIndex)) {
                    drawQuad((x + totalwidth), y,
                            (x + totalwidth + intObject.width),
                            (y + intObject.height), intObject.storedX,
                            intObject.storedY, intObject.storedX + intObject.width,
                            intObject.storedY + intObject.height);
                }
                totalwidth += intObject.width;
            }
        }

        GL11.glEnd();
    }

    public void drawString(float x, float y, String whatchars) {
        drawString(x, y, whatchars, Color.WHITE);
    }

}