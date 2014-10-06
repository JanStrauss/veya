package net.mschorn.eraseme;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;

public class Veya {
	public static final int RESTART = 0xFFFFFFFF;
	
	private static Camera camera;
	private static Shader shader;
	private static Scene scene;
	
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
		Display.create(new PixelFormat().withSamples(4), new ContextAttribs(3, 2));
		
		Veya.shader = new Shader(new String[] { "vertexPosition", "vertexColor" }, new String[] { "modelMatrix", "viewMatrix", "projectionMatrix" });
		Veya.camera = new Camera(Veya.shader, Display.getHeight(), Display.getWidth());
		Veya.scene = new Scene(Veya.shader);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Veya.RESTART);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	private static void run() {
		Veya.shader.use(true);
		Veya.camera.updateProjectionMatrix(60, Display.getWidth(), Display.getHeight(), 0.1f, 100f);
		Veya.camera.updateViewMatrix(5, 5, 5);
		Veya.shader.use(false);
		
		while (!Display.isCloseRequested()) {
			
			// Util.checkGLError();
			
			if (Display.wasResized()) {
				final int width = Display.getWidth();
				final int height = Display.getHeight();
				Veya.shader.use(true);
				Veya.camera.updateProjectionMatrix(60, width, height, 0.1f, 100.0f); // TODO
				Veya.shader.use(false);
				GL11.glViewport(0, 0, width, height);
				System.out.println("resized");
			}
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			Veya.shader.use(true);
			
			final float posX = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 5f;
			final float posY = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 2f + 2.5f;
			final float posZ = (float) Math.cos(System.currentTimeMillis() / 1000.0) * 5f;
			
			Veya.camera.updateViewMatrix(posX, posY, posZ);
			
			Veya.scene.render();
			
			Veya.shader.use(false);
			
			Display.update();
			Util.checkGLError();
		}
		
	}
	
	private static void end() {
		// TODO
	}
	
}
