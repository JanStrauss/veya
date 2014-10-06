package net.mschorn.eraseme;

import net.mschorn.eraseme.buffer.IBO;
import net.mschorn.eraseme.buffer.VBO;
import net.mschorn.eraseme.mvp.MVP;
import net.mschorn.eraseme.shader.Program;
import net.mschorn.eraseme.shader.Shader;
import net.mschorn.eraseme.state.VAO;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.PixelFormat;

public class Main {
	
	private static final int GL_MAJOR_VERSION = 3;
	private static final int GL_MINOR_VERSION = 2;
	
	private static final int RESTART = 0xFFFFFFFF;
	
	//@formatter:off
	 private static final int[] CUBE_IBO = new int[] {
		 
		 0, 1, 2, 3, Main.RESTART,
		 5, 1, 4, 0, Main.RESTART,
		 0, 2, 4, 6, Main.RESTART,
		 4, 6, 5, 7, Main.RESTART,
		 1, 5, 3, 7, Main.RESTART,
		 2, 3, 6, 7
	};
		  
	// x, y, z, r, g, b
	private static final float[] CUBE_VBO = new float[] {
		 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
		 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
		 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
		 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
		 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
		 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
		 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
		 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
		 };
	
	//@formatter:on
	
	private static int WIDTH = 1280;
	private static int HEIGHT = 720;
	private static float FOV = 60;
	
	private static final int MVP_BINDING = 0;
	private static final int V_LOCATION = 0;
	private static final int VC_LOCATION = 1;
	
	private static final String VS = "vs.glsl";
	private static final String FS = "fs.glsl";
	
	private final Shader vs;
	private final Shader fs;
	private final Program p;
	
	private final IBO ibo;
	private final VBO vbo;
	private final VAO vao;
	
	private final MVP mvp;
	
	public static void main(final String[] args) {
		final DisplayMode displayMode = new DisplayMode(Main.WIDTH, Main.HEIGHT);
		final PixelFormat pixelFormat = new PixelFormat().withSamples(4);
		final ContextAttribs contextAttribs = new ContextAttribs(Main.GL_MAJOR_VERSION, Main.GL_MINOR_VERSION).withProfileCore(true);
		
		new Main(displayMode, pixelFormat, contextAttribs).run();
		
	}
	
	public Main(final DisplayMode displayMode, final PixelFormat pixelFormat, final ContextAttribs contextAttribs) {
		
		this.initDisplay(displayMode, pixelFormat, contextAttribs);
		
		this.vs = new Shader(GL20.GL_VERTEX_SHADER, this.getClass().getResourceAsStream(Main.VS));
		this.fs = new Shader(GL20.GL_FRAGMENT_SHADER, this.getClass().getResourceAsStream(Main.FS));
		this.p = new Program(this.vs, this.fs);
		
		this.ibo = new IBO(Main.CUBE_IBO, GL15.GL_STATIC_DRAW);
		this.vbo = new VBO(Main.CUBE_VBO, GL15.GL_STATIC_DRAW);
		this.vao = new VAO(GL11.GL_TRIANGLE_STRIP, Main.CUBE_IBO.length, this.ibo, this.vbo);
		
		this.vao.addVertexAttribute(Main.V_LOCATION, 3, GL11.GL_FLOAT, false, 6 * 4, 0 * 4);
		this.vao.addVertexAttribute(Main.VC_LOCATION, 3, GL11.GL_FLOAT, false, 6 * 4, 3 * 4);
		
		this.mvp = new MVP(Main.MVP_BINDING, Main.WIDTH, Main.HEIGHT);
		
	}
	
	private void run() {
		
		this.glInit();
		
		while (!Display.isCloseRequested()) {
			if (Display.wasResized()) {
				Main.WIDTH = Display.getWidth();
				Main.HEIGHT = Display.getHeight();
				this.mvp.setPerspective(Main.FOV, (float) Main.WIDTH / (float) Main.HEIGHT, 0.1f, 100f);
				GL11.glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
				System.out.println("resized");
			}
			
			this.glDisplay();
			Display.update();
			
		}
		
		this.glDispose();
		Display.destroy();
		
	}
	
	private void initDisplay(final DisplayMode displayMode, final PixelFormat pixelFormat, final ContextAttribs contextAttribs) {
		
		try {
			
			Display.setDisplayMode(displayMode);
			Display.setTitle("Veya");
			Display.setResizable(true);
			Display.create(pixelFormat, contextAttribs);
			
		} catch (final LWJGLException e) {
			
			throw new RuntimeException(e);
			
		}
		
	}
	
	public void glInit() {
		
		// GL11.glClearColor(1, 0, 0, 0);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Main.RESTART);
		
		this.vs.glInit();
		this.fs.glInit();
		this.p.glInit();
		
		this.ibo.glInit();
		this.vbo.glInit();
		this.vao.glInit();
		
		this.mvp.glInit();
		
		this.mvp.setPerspective(Main.FOV, (float) Main.WIDTH / (float) Main.HEIGHT, 0.1f, 100f);
		
		GL11.glViewport(0, 0, Main.WIDTH, Main.HEIGHT);
	}
	
	public void glDisplay() {
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		final float posX = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 5f;
		final float posY = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 2f + 2.5f;
		final float posZ = (float) Math.cos(System.currentTimeMillis() / 1000.0) * 5f;
		
		this.mvp.setLookAt(posX, posY, posZ, 0, 0, 0, 0, 1, 0);
		this.mvp.setModelTranslation(-0.5f, 0, -0.5f);
		this.mvp.setModelScale(1, 1f, 1);
		
		this.p.glBind();
		this.mvp.glBind();
		this.vao.glDraw();
		
		this.mvp.setModelTranslation(-5, -10, -5);
		this.mvp.setModelScale(10f, 10f, 10f);
		
		this.mvp.glBind();
		this.vao.glDraw();
	}
	
	public void glDispose() {
		
		this.p.glDispose();
		this.fs.glDispose();
		this.vs.glDispose();
		
		this.vao.glDispose();
		this.vbo.glDispose();
		this.ibo.glDispose();
		
		this.mvp.glDispose();
		
	}
	
}
