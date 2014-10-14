package eu.over9000.veya;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;

public class Veya {
	public static final int RESTART = 0xFFFFFFFF;
	
	private static Camera camera;
	private static Program program;
	private static Scene scene;
	
	private static final float mouseSensitivity = 0.01f;
	private static final float movementSpeed = 10.0f; // move 10 units per second
	
	private static final float fieldOfView = 60.0f;
	private static final float nearClippingPlane = 0.1f;
	private static final float farClippingPlane = 1000.0f;
	
	private static boolean colorSwitch = false;
	
	public static void main(final String[] args) throws LWJGLException {
		
		System.setProperty("org.lwjgl.util.Debug", "true");
		
		System.out.println(Float.MAX_VALUE);
		
		Veya.init();
		Veya.run();
		Veya.end();
	}
	
	private static void init() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Veya");
		Display.setResizable(true);
		Display.create(new PixelFormat().withSamples(4), new ContextAttribs(3, 3));
		
		Veya.program = new Program(new String[] { "vertexPosition", "vertexColor", "vertexTexturePosition", "vertexNormal", }, new String[] { "modelMatrix", "viewMatrix", "projectionMatrix",
				"lightPosition", "lightColor", "colorSwitch" });
		
		Util.checkGLError();
		
		Veya.camera = new Camera(Veya.program, Display.getHeight(), Display.getWidth(), 15, 15, 15);
		Veya.scene = new Scene(Veya.program);
		
		Util.checkGLError();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		Util.checkGLError();
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Veya.RESTART);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GL11.glClearColor(124f / 255f, 169f / 255f, 255f / 255f, 1.0f);
		
		Mouse.setGrabbed(true);
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
				if (Keyboard.getEventKey() == Keyboard.KEY_C && Keyboard.getEventKeyState() == true) {
					Veya.colorSwitch = !Veya.colorSwitch;
					GL20.glUniform1i(Veya.program.getUniformLocation("colorSwitch"), Veya.colorSwitch ? 1 : 0);
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_X) {
					Mouse.setGrabbed(!Keyboard.getEventKeyState());
				}
			}
			
			final float posX = (float) Math.sin(System.currentTimeMillis() / 5000.0) * 255f;
			final float posY = (float) Math.cos(System.currentTimeMillis() / 5000.0) * 255f;
			// final float posZ = (float) Math.cos(System.currentTimeMillis() / 1500.0) * 20f;
			
			// final float kek = (posY / 255f + 1) / 2;
			
			// GL11.glClearColor(kek * 124f / 255f, kek * 169f / 255f, kek * 255f / 255f, 1.0f);
			
			Veya.camera.updateViewMatrix();
			Veya.scene.updateLight(posX, posY, 0);
			
			Veya.scene.render();
			
			Util.checkGLError();
			
			Veya.program.use(false);
			
			Display.update();
			Display.sync(60);
			Util.checkGLError();
			
			final long end = Sys.getTime();
			if (end - start > 1000) {
				start = end;
				Display.setTitle("VEYA | fps: " + count + " | pos: x=" + Veya.camera.getPosition().x + ", y=" + Veya.camera.getPosition().y + ", z=" + Veya.camera.getPosition().z);
				count = 0;
			} else {
				count++;
			}
			
		}
		
	}
	
	private static void end() {
		Veya.scene.dispose();
		Veya.program.unload();
	}
	
}
