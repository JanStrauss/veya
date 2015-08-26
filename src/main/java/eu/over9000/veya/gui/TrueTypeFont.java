/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.over9000.veya.gui;

import java.awt.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;

public class TrueTypeFont {

	/**
	 * Array that holds necessary information about the font characters
	 */
	private final CharDataContainer[] charData = new CharDataContainer[256];

	/**
	 * Map of user defined font characters (Character <-> IntObject)
	 */
	private final Map<Character, CharDataContainer> customChars = new HashMap<>();

	/**
	 * Boolean flag on whether AntiAliasing is enabled or not
	 */
	private final boolean antiAlias;

	/**
	 * Font's size
	 */
	private int fontSize = 0;

	/**
	 * Font's height
	 */
	private int fontHeight = 0;

	private int textureId;

	private final int textureWidth = 512;
	private final int textureHeight = 512;

	/**
	 * A reference to Java's AWT Font that we create our font texture from
	 */
	private final java.awt.Font font;

	/**
	 * This is a special internal class that holds our necessary information for
	 * the font characters. This includes width, height, and where the character
	 * is stored on the font texture.
	 */
	private class CharDataContainer {
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
	 * @param font
	 * 		Standard Java AWT font
	 * @param antiAlias
	 * 		Whether or not to apply AntiAliasing to the cached font
	 * @param additionalChars
	 * 		Characters of font that will be used in addition of first 256 (by unicode).
	 */
	public TrueTypeFont(final java.awt.Font font, final boolean antiAlias, final char[] additionalChars) {

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
	 * @param font
	 * 		Standard Java AWT font
	 * @param antiAlias
	 * 		Whether or not to apply AntiAliasing to the cached font
	 */
	public TrueTypeFont(final java.awt.Font font, final boolean antiAlias) {
		this(font, antiAlias, null);
	}

	/**
	 * Create a standard Java2D BufferedImage of the given character
	 *
	 * @param ch
	 * 		The character to create a BufferedImage for
	 * @return A BufferedImage containing the character
	 */
	private BufferedImage getFontImage(final char ch) {
		// Create a temporary image to extract the character's size
		final BufferedImage tempfontImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = tempfontImage.createGraphics();
		if (antiAlias) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setFont(font);
		/*
	  The font metrics for our Java AWT font
	 */
		final FontMetrics fontMetrics = g.getFontMetrics();
		int charwidth = fontMetrics.charWidth(ch);

		if (charwidth <= 0) {
			charwidth = 1;
		}
		int charheight = fontMetrics.getHeight();
		if (charheight <= 0) {
			charheight = fontSize;
		}

		// Create another image holding the character we are creating
		final BufferedImage fontImage;
		fontImage = new BufferedImage(charwidth, charheight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gt = fontImage.createGraphics();
		if (antiAlias) {
			gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		gt.setFont(font);

		gt.setColor(Color.WHITE);
		final int charx = 0;
		final int chary = 0;
		gt.drawString(String.valueOf(ch), (charx), (chary) + fontMetrics.getAscent());

		return fontImage;
	}

	/**
	 * Create and store the font
	 *
	 * @param customCharsArray
	 * 		Characters that should be also added to the cache.
	 */
	private void createSet(final char[] customCharsArray) {
		try {

			final BufferedImage imgTemp = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = imgTemp.createGraphics();

			g.setColor(new Color(255, 255, 255, 1));
			g.fillRect(0, 0, textureWidth, textureHeight);

			int rowHeight = 0;
			int positionX = 0;
			int positionY = 0;

			final int customCharsLength = (customCharsArray != null) ? customCharsArray.length : 0;

			for (int i = 0; i < 256 + customCharsLength; i++) {

				// get 0-255 characters and then custom characters
				final char ch = (i < 256) ? (char) i : customCharsArray != null ? customCharsArray[i - 256] : 0;

				final BufferedImage fontImage = getFontImage(ch);

				final CharDataContainer newCharDataContainer = new CharDataContainer();

				newCharDataContainer.width = fontImage.getWidth();
				newCharDataContainer.height = fontImage.getHeight();

				if (positionX + newCharDataContainer.width >= textureWidth) {
					positionX = 0;
					positionY += rowHeight;
					rowHeight = 0;
				}

				newCharDataContainer.storedX = positionX;
				newCharDataContainer.storedY = positionY;

				if (newCharDataContainer.height > fontHeight) {
					fontHeight = newCharDataContainer.height;
				}

				if (newCharDataContainer.height > rowHeight) {
					rowHeight = newCharDataContainer.height;
				}

				// Draw it here
				g.drawImage(fontImage, positionX, positionY, null);

				positionX += newCharDataContainer.width;

				if (i < 256) { // standard characters
					charData[i] = newCharDataContainer;
				} else { // custom characters
					customChars.put(ch, newCharDataContainer);
				}

			}

			buildTexture(imgTemp);

		} catch (final IOException e) {
			System.err.println("Failed to create font.");
			e.printStackTrace();
		}
	}

	private void buildTexture(final BufferedImage imgTemp) throws IOException {
		textureId = GL11.glGenTextures();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(imgTemp, "png", byteArrayOutputStream);
		final PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
		final ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
		buffer.flip();

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	private void drawQuad(final float drawX, final float drawY, final float drawX2, final float drawY2, final float srcX, final float srcY, final float srcX2, final float srcY2) {
		final float DrawWidth = drawX2 - drawX;
		final float DrawHeight = drawY2 - drawY;
		final float TextureSrcX = srcX / textureWidth;
		final float TextureSrcY = srcY / textureHeight;
		final float SrcWidth = srcX2 - srcX;
		final float SrcHeight = srcY2 - srcY;
		final float RenderWidth = (SrcWidth / textureWidth);
		final float RenderHeight = (SrcHeight / textureHeight);

		GL11.glTexCoord2f(TextureSrcX, TextureSrcY);
		GL11.glVertex2f(drawX, drawY);
		GL11.glTexCoord2f(TextureSrcX, TextureSrcY + RenderHeight);
		GL11.glVertex2f(drawX, drawY + DrawHeight);
		GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY + RenderHeight);
		GL11.glVertex2f(drawX + DrawWidth, drawY + DrawHeight);
		GL11.glTexCoord2f(TextureSrcX + RenderWidth, TextureSrcY);
		GL11.glVertex2f(drawX + DrawWidth, drawY);
	}

	public int getWidth(final String whatchars) {
		int totalwidth = 0;
		CharDataContainer charDataContainer;
		int currentChar;
		for (int i = 0; i < whatchars.length(); i++) {
			currentChar = whatchars.charAt(i);
			if (currentChar < 256) {
				charDataContainer = charData[currentChar];
			} else {
				charDataContainer = customChars.get((char) currentChar);
			}

			if (charDataContainer != null) {
				totalwidth += charDataContainer.width;
			}
		}
		return totalwidth;
	}

	public int getHeight() {
		return fontHeight;
	}

	public void drawString(final float x, final float y, final String whatchars, final Color color) {
		drawString(x, y, whatchars, color, 0, whatchars.length() - 1);
	}

	public void drawString(final float x, final float y, final String whatchars, final Color color, final int startIndex, final int endIndex) {
		GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		CharDataContainer charDataContainer;
		int charCurrent;

		GL11.glBegin(GL11.GL_QUADS);

		int totalwidth = 0;
		for (int i = 0; i < whatchars.length(); i++) {
			charCurrent = whatchars.charAt(i);
			if (charCurrent < 256) {
				charDataContainer = charData[charCurrent];
			} else {
				charDataContainer = customChars.get((char) charCurrent);
			}

			if (charDataContainer != null) {
				if ((i >= startIndex) || (i <= endIndex)) {
					drawQuad((x + totalwidth), y, (x + totalwidth + charDataContainer.width), (y + charDataContainer.height), charDataContainer.storedX, charDataContainer.storedY, charDataContainer.storedX + charDataContainer.width, charDataContainer.storedY + charDataContainer.height);
				}
				totalwidth += charDataContainer.width;
			}
		}

		GL11.glEnd();
	}

	public void drawString(final float x, final float y, final String whatchars) {
		drawString(x, y, whatchars, Color.WHITE);
	}

}