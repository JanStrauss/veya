package eu.over9000.veya;

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
	private static Program program;
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
		
		Veya.program = new Program(new String[] { "vertexPosition", "vertexColor", "vertexTexturePosition" }, new String[] { "modelMatrix", "viewMatrix", "projectionMatrix" });
		Veya.camera = new Camera(Veya.program, Display.getHeight(), Display.getWidth());
		Veya.scene = new Scene(Veya.program);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Veya.RESTART);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}
	
	private static void run() {
		Veya.program.use(true);
		Veya.camera.updateProjectionMatrix(60, Display.getWidth(), Display.getHeight(), 0.1f, 100f);
		Veya.program.use(false);
		
		long start = System.currentTimeMillis();
		long count = 0;
		while (!Display.isCloseRequested()) {
			
			// Util.checkGLError();
			
			if (Display.wasResized()) {
				final int width = Display.getWidth();
				final int height = Display.getHeight();
				Veya.program.use(true);
				Veya.camera.updateProjectionMatrix(60, width, height, 0.1f, 100.0f); // TODO
				Veya.program.use(false);
				GL11.glViewport(0, 0, width, height);
				System.out.println("resized");
			}
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			Veya.program.use(true);
			
			final float posX = (float) Math.sin(System.currentTimeMillis() / 1500.0) * 15f;
			final float posY = (float) Math.sin(System.currentTimeMillis() / 1500.0) * 7f;
			final float posZ = (float) Math.cos(System.currentTimeMillis() / 1500.0) * 15f;
			
			Veya.camera.updateViewMatrix(posX, posY, posZ);
			
			Veya.scene.render();
			
			Veya.program.use(false);
			
			Display.update();
			Util.checkGLError();
			
			final long end = System.currentTimeMillis();
			if (end - start > 1000) {
				start = end;
				System.out.println("fps: " + count);
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
