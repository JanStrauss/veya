/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.over9000.veya.gui;

import java.awt.*;
import java.awt.event.KeyEvent;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class Gui {

	private static final StringBuilder renderString = new StringBuilder("Enter your text");

	private static TrueTypeFont font;
	private static int cnt = 0;
	
	public static void main(final String[] args) {
		setUpStates(1280, 720);

		setUpFonts();

		enterGameLoop();
		cleanUp();
	}

	private static void setUpFonts() {
		font = new TrueTypeFont(new Font("Arial", Font.PLAIN, 16), true, new char[]{'€'});
	}

	private static void setUpStates(final int width, final int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));

			Display.create();
			//Display.create(new PixelFormat().withSamples(4).withDepthBits(24), new ContextAttribs(3, 3));
			Display.setVSyncEnabled(true);
		} catch (final LWJGLException e) {
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
		font.drawString(5, 5, renderString.toString() + ' ' + cnt++ + " €", Color.WHITE);
	}

	private static void input() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				// Reset the string if we press escape.
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					renderString.setLength(0);
					continue;
				}
				// Append the pressed key to the string if the key isn't the back key or the shift key.
				if (Keyboard.getEventKey() != Keyboard.KEY_BACK) {
					if (isPrintableChar(Keyboard.getEventCharacter())) {
						renderString.append(Keyboard.getEventCharacter());
					}
				} else if (renderString.length() > 0) {
					renderString.setLength(renderString.length() - 1);
				}
			}
		}
	}

	public static boolean isPrintableChar(final char c) {
		final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	private static void update() {
		Display.update();
		Display.sync(60);
	}

	private static void cleanUp() {
		Display.destroy();
		System.exit(0);
	}
}