package eu.over9000.veya;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;

public class Veya {
	public static final int RESTART = 0xFFFFFFFF;

	private static Camera camera;
	private static Program program;
	private static Scene scene;

	private static final float mouseSensitivity = 0.01f;
	private static final float movementSpeed = 10.0f;

	private static final float fieldOfView = 75.0f;
	private static final float nearClippingPlane = 0.1f;
	private static final float farClippingPlane = 1000.0f;

	private static boolean colorSwitch = false;

	private static float ambient = 0.70f;
	private static float diffuse = 0.55f;
	private static float specular = 0.05f;
	private static final float df = 0.05f;

	public static void main(final String[] args) throws LWJGLException {

		System.setProperty("org.lwjgl.util.Debug", "true");

		Veya.init();
		Veya.run();
		Veya.end();
	}

	private static void init() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Veya");
		Display.setResizable(true);
		Display.create(new PixelFormat().withSamples(4).withDepthBits(24), new ContextAttribs(3, 3));

		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		System.out.println("Java version: " + System.getProperty("java.version"));

		Veya.program = new Program(new String[]{"vertexPosition", "vertexColor", "vertexTexturePosition", "vertexNormal",}, new String[]{"modelMatrix", "viewMatrix", "projectionMatrix", "lightPosition", "lightColor", "lightFactors", "colorSwitch", "cameraPosition"});

		Util.checkGLError();

		Veya.camera = new Camera(Veya.program, 60, 90, 60);
		Veya.scene = new Scene(Veya.program, Veya.camera);

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
		Veya.camera.updateProjectionMatrix(Veya.fieldOfView, Display.getWidth(), Display.getHeight(), Veya.nearClippingPlane, Veya.farClippingPlane);
		Veya.camera.updateViewMatrix();
		Veya.scene.init();

		Veya.program.use(false);

		Util.checkGLError();

		long start = Sys.getTime();
		long lastTime = start;
		long count = 0;

		while (!Display.isCloseRequested()) {
			final long time = Sys.getTime();
			final float dt = (time - lastTime) / 1000.0f;
			lastTime = time;

			if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
				Veya.camera.walkForward(Veya.movementSpeed * dt);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
				Veya.camera.walkBackwards(Veya.movementSpeed * dt);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				Veya.camera.strafeLeft(Veya.movementSpeed * dt);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				Veya.camera.strafeRight(Veya.movementSpeed * dt);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				Veya.camera.moveUp(Veya.movementSpeed * dt);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				Veya.camera.moveDown(Veya.movementSpeed * dt);
			}

			if (!Keyboard.isKeyDown(Keyboard.KEY_X)) {
				final float dx = Mouse.getDX();
				final float dy = Mouse.getDY();

				Veya.camera.yaw(dx * Veya.mouseSensitivity);
				Veya.camera.pitch(dy * Veya.mouseSensitivity);
			}

			while (Mouse.next()) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {

					scene.performLeftClick();
				}
				if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
					scene.performRightClick();

				}
			}

			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				return;
			}

			Util.checkGLError();

			if (Display.wasResized()) {
				Veya.program.use(true);
				Veya.camera.updateProjectionMatrix(Veya.fieldOfView, Display.getWidth(), Display.getHeight(), Veya.nearClippingPlane, Veya.farClippingPlane);
				Veya.program.use(false);
				GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
				System.out.println("resized");
			}

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			Veya.program.use(true);

			while (Keyboard.next()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState()) {
					Veya.colorSwitch = !Veya.colorSwitch;
					GL20.glUniform1i(Veya.program.getUniformLocation("colorSwitch"), Veya.colorSwitch ? 1 : 0);
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
			}

			final float posX = (float) Math.sin(System.currentTimeMillis() / 5000.0);
			final float posY = (float) Math.cos(System.currentTimeMillis() / 5000.0);
			// final float posZ = (float) Math.cos(System.currentTimeMillis() / 1500.0) * 20f;

			// final float kek = (posY / 1024f + 1) / 2;

			// GL11.glClearColor(kek * 124f / 255f, kek * 169f / 255f, kek * 255f / 255f, 1.0f);


			try {
				Vector3f camPos = camera.getPosition();
				float camY = scene.getWorld().getHighestYAt((int) Math.floor(camPos.x), (int) Math.floor(camPos.z)) + 2.7f;
				//Veya.camera.setPosition(camPos.x, camY, camPos.z);
			} catch (IllegalStateException e) {
			}


			Veya.camera.updateViewMatrix();
			Veya.camera.updateCameraPosition();
			// Veya.scene.updateLight(posX + Veya.camera.getPosition().getX(), posY, 0 + Veya.camera.getPosition().getZ());
			Veya.scene.getLight().updateLightPosition(0, 200, 0);
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
				Display.setTitle("VEYA | fps: " + count + " | pos: x=" + Veya.camera.getPosition().x + ", y=" + Veya.camera.getPosition().y + ", z=" + Veya.camera.getPosition().z + " | #chunks displayed: " + Veya.scene.getChunkCount() + " | lightFactors: A=" + Veya.ambient + ", D=" + Veya.diffuse + ", S=" + Veya.specular);
				count = 0;
			} else {
				count++;
			}

		}

	}

	private static void end() {
		Veya.scene.dispose();
		Veya.program.unload();
		Display.destroy();
		System.exit(0);
	}

}
