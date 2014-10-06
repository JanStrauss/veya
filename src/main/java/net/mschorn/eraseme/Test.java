package net.mschorn.eraseme;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.Util;

public class Test {
	public static final int RESTART = 0xFFFFFFFF;
	
	private static Camera camera;
	private static Shader shader;
	private static Scene scene;
	
	public static void main(final String[] args) throws LWJGLException {
		System.setProperty("org.lwjgl.util.Debug", "true");
		
		Test.init();
		Test.run();
		Test.end();
	}
	
	private static void init() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("Veya");
		Display.setResizable(true);
		Display.create(new PixelFormat(), new ContextAttribs(3, 2));
		
		Test.shader = new Shader(new String[] { "vertexPosition", "vertexColor" }, new String[] { "modelMatrix", "viewMatrix", "projectionMatrix" });
		Test.camera = new Camera(Test.shader, Display.getHeight(), Display.getWidth());
		Test.scene = new Scene(Test.shader);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Test.RESTART);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	private static void run() {
		Test.shader.use(true);
		Test.camera.updateProjectionMatrix(60, Display.getWidth(), Display.getHeight(), 0.1f, 100f);
		Test.camera.updateViewMatrix(5, 5, 5);
		Test.shader.use(false);
		
		while (!Display.isCloseRequested()) {
			
			// Util.checkGLError();
			
			if (Display.wasResized()) {
				final int width = Display.getWidth();
				final int height = Display.getHeight();
				Test.shader.use(true);
				Test.camera.updateProjectionMatrix(60, width, height, 0.1f, 100.0f); // TODO
				Test.shader.use(false);
				GL11.glViewport(0, 0, width, height);
				System.out.println("resized");
			}
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			Test.shader.use(true);
			
			Test.scene.render();
			
			Test.shader.use(false);
			
			Display.update();
			Util.checkGLError();
		}
		
	}
	
	private static void end() {
		// TODO
	}
	
}
