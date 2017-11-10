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

package eu.over9000.veya;

import java.awt.*;
import java.io.IOException;
import java.util.EnumSet;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

import eu.over9000.veya.console.Console;
import eu.over9000.veya.rendering.Camera;
import eu.over9000.veya.rendering.Program;
import eu.over9000.veya.rendering.Scene;
import eu.over9000.veya.util.MathUtil;
import eu.over9000.veya.world.BlockType;

public class Veya {
	public static final int RESTART = 0xFFFFFFFF;

	public static boolean ENABLE_DAY_NIGHT = false;
	public static final float MOVEMENT_MULTIPLIER_WALK = 4.5f;

	public static final float MOVEMENT_MULTIPLIER_FLY = 25f;
	private static final float MOUSE_SENSITIVITY = 0.01f;

	public static final float FIELD_OF_VIEW = 75.0f;
	public static final float NEAR_CLIPPING_PLANE = 0.5f;
	public static final float FAR_CLIPPING_PLANE = 1000.0f;

	public static Camera camera;

	public static Program program_normal;
	public static Program program_shadow;
	public static Program program_debug;

	public static Scene scene;
	public static boolean colorSwitch = false;

	public static boolean aoSwitch = true;
	public static boolean gravitySwitch = true;
	public static boolean collisionSwitch = true;
	public static boolean wireframeSwitch = false;

	private static float ambient = 0.60f;
	private static float diffuse = 0.40f;

	private static float specular = 0.30f;
	private static final float df = 0.05f;

	private static Frame frame;
	private static boolean shutdown = false;

	public static boolean debugShadow = false;

	private static long lastSpacePress = 0;
	public static final EnumSet<BlockType> ignoreBlocks = EnumSet.noneOf(BlockType.class);

	//public static final EnumSet<BlockType> ignoreBlocks = EnumSet.of(BlockType.STONE);

	public static void main(final String[] args) throws LWJGLException {

		System.setProperty("org.lwjgl.util.Debug", "true");

		Veya.init();
		Veya.run();
		Veya.end();
	}

	private static void init() throws LWJGLException {
		final Canvas canvas = new Canvas();
		frame = new Frame();
		frame.add(canvas);
		frame.setSize(1440, 900);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setIconImage(loadIcon());

		Display.create(new PixelFormat(), new ContextAttribs(3, 3).withProfileCore(true));

		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("graphics adapter: " + Display.getAdapter());

		Veya.program_normal = new Program("normal", new String[]{"modelMatrix", "viewMatrix", "projectionMatrix", "lightPosition", "lightColor", "lightFactors", "colorSwitch", "aoSwitch", "cameraPosition", "lightSpaceMatrix", "textureData", "shadowMap"});
		Veya.program_shadow = new Program("shadow", new String[]{"modelMatrix", "lightSpaceMatrix"});
		Veya.program_debug = new Program("debug", new String[]{"near_plane", "far_plane"});

		Util.checkGLError();

		Veya.camera = new Camera(-40, 120, -40);
		Veya.scene = new Scene(1337);

		Util.checkGLError();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

		Util.checkGLError();

		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(Veya.RESTART);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GL11.glClearColor(124f / 255f, 169f / 255f, 255f / 255f, 1.0f);

		Mouse.setGrabbed(true);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Console.start();
	}

	private static void run() {
		Util.checkGLError();

		Veya.program_normal.use(true);
		Veya.camera.updateProjectionMatrix(Display.getWidth(), Display.getHeight());
		Veya.camera.updateViewMatrix();
		Veya.scene.init();


		Veya.program_normal.use(false);

		program_normal.use(true);
		GL20.glUniform1i(Veya.program_normal.getUniformLocation("colorSwitch"), Veya.colorSwitch ? 1 : 0);
		program_normal.use(false);

		program_normal.use(true);
		GL20.glUniform1i(Veya.program_normal.getUniformLocation("aoSwitch"), Veya.aoSwitch ? 1 : 0);
		program_normal.use(false);


		Util.checkGLError();

		long start = Sys.getTime();
		long lastTime = start;
		long count = 0;

		while (!Display.isCloseRequested() && !shutdown) {
			final long time = Sys.getTime();
			final float dt = (time - lastTime) / 1000.0f;
			lastTime = time;

			checkResize();

			handleMovementKeys(dt);
			handleOtherKeys();
			handleMouseInput();

			if (gravitySwitch) {
				Veya.camera.applyGravity(dt);
			}

			Veya.camera.performMove();


			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			Veya.program_normal.use(true);


			if (ENABLE_DAY_NIGHT) {
				final float posX = (float) Math.sin(System.currentTimeMillis() / 10000.0) * 512;
				final float posY = (float) Math.cos(System.currentTimeMillis() / 10000.0) * 512;
				// final float posZ = (float) Math.cos(System.currentTimeMillis() / 1500.0) * 20f;
				ambient = 0.25f + MathUtil.scale(posY, -1024, 1024, 0, 0.55f);
				diffuse = MathUtil.scale(posY, -1024, 1024, 0, 0.5f);
				specular = MathUtil.scale(posY, -1024, 1024, 0, 0.5f);
				final float kek = (posY / 1024f + 1) / 2;

				GL11.glClearColor(kek * 124f / 255f, kek * 169f / 255f, kek * 255f / 255f, 1.0f);
				Veya.scene.getLight().updateLightPosition(posX + Veya.camera.getPosition().getX(), posY, 1 + Veya.camera.getPosition().getZ());
			}

			//Veya.scene.getLight().updateLightPosition(Veya.camera.getPosition().getX() + 1, 500, Veya.camera.getPosition().getZ() + 250);

			Veya.camera.updateViewMatrix();
			Veya.camera.updateCameraPosition();
			Veya.scene.getLight().updateLightFactors(Veya.ambient, Veya.diffuse, Veya.specular);
			Veya.scene.render();

			Util.checkGLError();

			Veya.program_normal.use(false);

			Display.update();
			Display.sync(60);
			Util.checkGLError();

			final long end = Sys.getTime();
			if (end - start > 1000) {
				start = end;
				frame.setTitle("VEYA | fps: " + count + " | pos: x=" + Veya.camera.getPosition().x + ", y=" + Veya.camera.getPosition().y + ", z=" + Veya.camera.getPosition().z + " | #chunks displayed: " + Veya.scene.getChunkCount() + " | lightFactors: A=" + Veya.ambient + ", D=" + Veya.diffuse + ", S=" + Veya.specular + " | chunk updates: " + scene.chunkUpdateCounterInRender.get() + "/" + scene.chunkUpdateCounterOffRender.get() + " | selected block: " + scene.placeBlockType);
				count = 0;
				scene.chunkUpdateCounterInRender.set(0);
				scene.chunkUpdateCounterOffRender.set(0);

			} else {
				count++;
			}
		}
	}

	private static void handleOtherKeys() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
				Veya.colorSwitch = !Veya.colorSwitch;
				program_normal.use(true);
				GL20.glUniform1i(Veya.program_normal.getUniformLocation("colorSwitch"), Veya.colorSwitch ? 1 : 0);
				program_normal.use(false);
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_Y && Keyboard.getEventKeyState()) {
				Veya.aoSwitch = !Veya.aoSwitch;
				program_normal.use(true);
				GL20.glUniform1i(Veya.program_normal.getUniformLocation("aoSwitch"), Veya.aoSwitch ? 1 : 0);
				program_normal.use(false);
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_V && Keyboard.getEventKeyState()) {
				Veya.wireframeSwitch = !Veya.wireframeSwitch;
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_Q && Keyboard.getEventKeyState()) {
				Veya.debugShadow = !Veya.debugShadow;
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_SPACE && Keyboard.getEventKeyState()) {
				final long diff = Keyboard.getEventNanoseconds() - lastSpacePress;
				if (diff < 250 * 1000000) {
					Veya.gravitySwitch = !Veya.gravitySwitch;
					camera.resetVelocity();
				}
				lastSpacePress = Keyboard.getEventNanoseconds();
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_X) {
				Mouse.setGrabbed(!Keyboard.getEventKeyState());
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_PRIOR && Keyboard.getEventKeyState()) {
				if (Veya.specular + Veya.df <= 1.0f) {
					Veya.specular += Veya.df;
				} else {
					Veya.specular = 1.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_NEXT && Keyboard.getEventKeyState()) {
				if (Veya.specular - Veya.df >= 0.0f) {
					Veya.specular -= Veya.df;
				} else {
					Veya.specular = 0.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_HOME && Keyboard.getEventKeyState()) {
				if (Veya.diffuse + Veya.df <= 1.0f) {
					Veya.diffuse += Veya.df;
				} else {
					Veya.diffuse = 1.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_END && Keyboard.getEventKeyState()) {
				if (Veya.diffuse - Veya.df >= 0.0f) {
					Veya.diffuse -= Veya.df;
				} else {
					Veya.diffuse = 0.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_INSERT && Keyboard.getEventKeyState()) {
				if (Veya.ambient + Veya.df <= 1.0f) {
					Veya.ambient += Veya.df;
				} else {
					Veya.ambient = 1.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_DELETE && Keyboard.getEventKeyState()) {
				if (Veya.ambient - Veya.df >= 0.0f) {
					Veya.ambient -= Veya.df;
				} else {
					Veya.ambient = 0.0f;
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && Keyboard.getEventKeyState()) {
				shutdown = true;
			}
		}
	}

	private static void handleMovementKeys(final float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			Veya.camera.tryWalkForward(Veya.getMovementMultiplier() * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			Veya.camera.tryWalkBackwards(Veya.getMovementMultiplier() * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			Veya.camera.tryStrafeLeft(Veya.getMovementMultiplier() * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			Veya.camera.tryStrafeRight(Veya.getMovementMultiplier() * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			Veya.camera.tryMoveUp(Veya.getMovementMultiplier() * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			Veya.camera.tryMoveDown(Veya.getMovementMultiplier() * dt);
		}
	}

	private static void checkResize() {
		if (Display.wasResized()) {
			Veya.program_normal.use(true);
			Veya.camera.updateProjectionMatrix(Display.getWidth(), Display.getHeight());
			Veya.program_normal.use(false);
			GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
			System.out.println("resized");
		}
	}

	private static void handleMouseInput() {
		if (!Keyboard.isKeyDown(Keyboard.KEY_X)) {
			final float dx = Mouse.getDX();
			final float dy = Mouse.getDY();

			Veya.camera.yaw(dx * Veya.MOUSE_SENSITIVITY);
			Veya.camera.pitch(dy * Veya.MOUSE_SENSITIVITY);
		}
		while (Mouse.next()) {
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
				scene.performLeftClick();
			}
			if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
				scene.performRightClick();
			}
			if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
				scene.performMiddleClick();
			}
		}
	}

	private static void end() {
		Veya.scene.dispose();
		Veya.program_normal.unload();
		Display.destroy();
		frame.setVisible(false);

		Console.stop();

		System.exit(0);
	}

	private static Image loadIcon() {
		try {
			return ImageIO.read(Veya.class.getResourceAsStream("/icon/veya.png"));
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static float getMovementMultiplier() {
		return gravitySwitch ? MOVEMENT_MULTIPLIER_WALK : MOVEMENT_MULTIPLIER_FLY;
	}
}
