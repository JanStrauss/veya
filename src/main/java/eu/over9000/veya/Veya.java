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

import eu.over9000.veya.rendering.Camera;
import eu.over9000.veya.rendering.Program;
import eu.over9000.veya.rendering.Scene;
import eu.over9000.veya.util.MathUtil;
import eu.over9000.veya.world.BlockType;

public class Veya {
	public static final int RESTART = 0xFFFFFFFF;
	public static final boolean ENABLE_DAY_NIGHT = false;
	public static final boolean ENABLE_COLLISION = true;

	public static final float MOVEMENT_MULTIPLIER_WALK = 4.5f;
	public static final float MOVEMENT_MULTIPLIER_FLY = 25f;

	private static final float MOUSE_SENSITIVITY = 0.01f;

	public static final float FIELD_OF_VIEW = 75.0f;
	public static final float NEAR_CLIPPING_PLANE = 0.1f;
	public static final float FAR_CLIPPING_PLANE = 1000.0f;

	public static Camera camera;
	public static Program program;
	public static Scene scene;

	public static boolean colorSwitch = false;
	public static boolean gravitySwitch = true;

	private static float ambient = 0.75f;
	private static float diffuse = 0.50f;
	private static float specular = 0.50f;
	private static final float df = 0.05f;
	private static Frame frame;

	private static boolean shutdown = false;
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
		frame.setSize(1280, 720);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setIconImage(loadIcon());

		Display.setParent(canvas);
		Display.create(new PixelFormat().withSamples(4).withDepthBits(24), new ContextAttribs(3, 3));

		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Java version: " + System.getProperty("java.version"));
		System.out.println("graphics adapter: " + Display.getAdapter());

		Veya.program = new Program(new String[]{"vertexPosition", "vertexColor", "vertexTexturePosition", "vertexNormal",}, new String[]{"modelMatrix", "viewMatrix", "projectionMatrix", "lightPosition", "lightColor", "lightFactors", "colorSwitch", "cameraPosition"});

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

	}

	private static void run() {
		Util.checkGLError();

		Veya.program.use(true);
		Veya.camera.updateProjectionMatrix(Display.getWidth(), Display.getHeight());
		Veya.camera.updateViewMatrix();
		Veya.scene.init();

		Veya.program.use(false);

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

			Veya.program.use(true);


			if (ENABLE_DAY_NIGHT) {
				final float posX = (float) Math.sin(System.currentTimeMillis() / 10000.0) * 1024;
				final float posY = (float) Math.cos(System.currentTimeMillis() / 10000.0) * 1024;
				// final float posZ = (float) Math.cos(System.currentTimeMillis() / 1500.0) * 20f;
				ambient = 0.25f + MathUtil.scale(posY, -1024, 1024, 0, 0.55f);
				diffuse = MathUtil.scale(posY, -1024, 1024, 0, 0.5f);
				specular = MathUtil.scale(posY, -1024, 1024, 0, 0.5f);
				final float kek = (posY / 1024f + 1) / 2;

				GL11.glClearColor(kek * 124f / 255f, kek * 169f / 255f, kek * 255f / 255f, 1.0f);
				Veya.scene.getLight().updateLightPosition(posX + Veya.camera.getPosition().getX(), posY, 0 + Veya.camera.getPosition().getZ());
			}

			Veya.camera.updateViewMatrix();
			Veya.camera.updateCameraPosition();
			Veya.scene.getLight().updateLightFactors(Veya.ambient, Veya.diffuse, Veya.specular);
			Veya.scene.render();

			Util.checkGLError();

			Veya.program.use(false);

			Display.update();
			// Display.sync(60);
			Util.checkGLError();

			final long end = Sys.getTime();
			if (end - start > 1000) {
				start = end;
				frame.setTitle("VEYA | fps: " + count + " | pos: x=" + Veya.camera.getPosition().x + ", y=" + Veya.camera.getPosition().y + ", z=" + Veya.camera.getPosition().z + " | #chunks displayed: " + Veya.scene.getChunkCount() + " | lightFactors: A=" + Veya.ambient + ", D=" + Veya.diffuse + ", S=" + Veya.specular + " | chunk updates: " + scene.chunkUpdateCounter);
				count = 0;
				scene.chunkUpdateCounter = 0;

			} else {
				count++;
			}
		}
	}

	private static void handleOtherKeys() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
				Veya.colorSwitch = !Veya.colorSwitch;
				program.use(true);
				GL20.glUniform1i(Veya.program.getUniformLocation("colorSwitch"), Veya.colorSwitch ? 1 : 0);
				program.use(false);
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
			Veya.program.use(true);
			Veya.camera.updateProjectionMatrix(Display.getWidth(), Display.getHeight());
			Veya.program.use(false);
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
		}
	}

	private static void end() {
		Veya.scene.dispose();
		Veya.program.unload();
		Display.destroy();
		frame.setVisible(false);
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
