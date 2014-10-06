package eu.over9000.veya.asdf;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.asdf.buffer.IBO;
import eu.over9000.veya.asdf.buffer.VBO;
import eu.over9000.veya.asdf.mvp.MVP;
import eu.over9000.veya.asdf.shader.Program;
import eu.over9000.veya.asdf.shader.Shader;
import eu.over9000.veya.asdf.state.VAO;

public class Main {
	
	private static final int GL_MAJOR_VERSION = 3;
	private static final int GL_MINOR_VERSION = 3;
	
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
		 -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
		 -1.0f, -1.0f, +1.0f, 0.0f, 0.0f, 1.0f,
		 -1.0f, +1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
		 -1.0f, +1.0f, +1.0f, 0.0f, 1.0f, 1.0f,
		 +1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
		 +1.0f, -1.0f, +1.0f, 1.0f, 0.0f, 1.0f,
		 +1.0f, +1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
		 +1.0f, +1.0f, +1.0f, 1.0f, 1.0f, 1.0f,
		 };
	
	//@formatter:on
	
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	
	private static final int MVP_BINDING = 0;
	private static final int V_LOCATION = 0;
	private static final int VC_LOCATION = 1;
	
	private static final String VERTEX_SHADER = "vs.glsl";
	private static final String FRAGMENT_SHADER = "fs.glsl";
	
	private final Shader vertexShader;
	private final Shader fragmentShader;
	private final Program program;
	
	private final IBO indicesBufferObject;
	private final VBO vbo;
	private final VAO vao;
	
	private final MVP mvp;
	
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Matrix4f modelMatrix;
	
	private int projectionMatrixLocation;
	private int viewMatrixLocation;
	private int modelMatrixLocation;
	
	public static void main(final String[] args) {
		
		final DisplayMode displayMode = new DisplayMode(Main.WIDTH, Main.HEIGHT);
		final PixelFormat pixelFormat = new PixelFormat().withSamples(2);
		final ContextAttribs contextAttribs = new ContextAttribs(Main.GL_MAJOR_VERSION, Main.GL_MINOR_VERSION).withProfileCore(true);
		
		new Main(displayMode, pixelFormat, contextAttribs).run();
		
	}
	
	public Main(final DisplayMode displayMode, final PixelFormat pixelFormat, final ContextAttribs contextAttribs) {
		
		this.initDisplay(displayMode, pixelFormat, contextAttribs);
		
		this.vertexShader = new Shader(GL20.GL_VERTEX_SHADER, this.getClass().getResourceAsStream(Main.VERTEX_SHADER));
		this.fragmentShader = new Shader(GL20.GL_FRAGMENT_SHADER, this.getClass().getResourceAsStream(Main.FRAGMENT_SHADER));
		this.program = new Program(this.vertexShader, this.fragmentShader);
		
		this.indicesBufferObject = new IBO(Main.CUBE_IBO, GL15.GL_STATIC_DRAW);
		this.vbo = new VBO(Main.CUBE_VBO, GL15.GL_STATIC_DRAW);
		this.vao = new VAO(GL11.GL_TRIANGLE_STRIP, Main.CUBE_IBO.length, this.indicesBufferObject, this.vbo);
		
		this.vao.addVertexAttribute(Main.V_LOCATION, 3, GL11.GL_FLOAT, false, 6 * 4, 0 * 4);
		this.vao.addVertexAttribute(Main.VC_LOCATION, 3, GL11.GL_FLOAT, false, 6 * 4, 3 * 4);
		
		this.mvp = new MVP(Main.MVP_BINDING, Main.WIDTH, Main.HEIGHT);
		
		this.exitOnGLError("const");
		
	}
	
	private void exitOnGLError(final String errorMessage) {
		final int errorValue = GL11.glGetError();
		
		if (errorValue != GL11.GL_NO_ERROR) {
			final String errorString = GLU.gluErrorString(errorValue);
			System.err.println("ERROR - " + errorMessage + ": " + errorString);
			
			if (Display.isCreated()) {
				Display.destroy();
			}
			System.exit(-1);
		}
	}
	
	private void run() {
		
		this.glInit();
		this.exitOnGLError("init");
		
		while (!Display.isCloseRequested()) {
			
			this.glDisplay();
			Display.update();
			Display.sync(120);
		}
		
		this.glDispose();
		Display.destroy();
		
	}
	
	private void initDisplay(final DisplayMode displayMode, final PixelFormat pixelFormat, final ContextAttribs contextAttribs) {
		
		try {
			
			Display.setDisplayMode(displayMode);
			Display.setTitle("Veya");
			Display.create(pixelFormat, contextAttribs);
			
		} catch (final LWJGLException e) {
			
			throw new RuntimeException(e);
			
		}
		
	}
	
	public void glInit() {
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
		GL31.glPrimitiveRestartIndex(0xFFFFFFFF & Main.RESTART);
		
		this.vertexShader.glInit();
		this.fragmentShader.glInit();
		this.program.glInit();
		
		this.indicesBufferObject.glInit();
		this.vbo.glInit();
		this.vao.glInit();
		
		this.mvp.glInit();
		
		this.mvp.setPerspective(60.0f, (float) Main.WIDTH / (float) Main.HEIGHT, 0.1f, 100f);
		
		this.modelMatrixLocation = GL20.glGetUniformLocation(this.program.handle, "modelMatrix");
		this.viewMatrixLocation = GL20.glGetUniformLocation(this.program.handle, "viewMatrix");
		this.projectionMatrixLocation = GL20.glGetUniformLocation(this.program.handle, "projectionMatrix");
		
	}
	
	private void updateProjectionMatrix() {
		// Setup projection matrix
		this.projectionMatrix = new Matrix4f();
		final float fieldOfView = 60f;
		final float aspectRatio = (float) Main.WIDTH / (float) Main.HEIGHT;
		final float near_plane = 0.1f;
		final float far_plane = 100f;
		
		final float y_scale = (float) (1.0f / Math.tan(Math.toRadians(fieldOfView / 2.0f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = far_plane - near_plane;
		this.projectionMatrix.m00 = x_scale;
		this.projectionMatrix.m11 = y_scale;
		this.projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		this.projectionMatrix.m23 = -1;
		this.projectionMatrix.m32 = -(2 * near_plane * far_plane / frustum_length);
		this.projectionMatrix.m33 = 0;
		
		this.projectionMatrix.store(this.matrixBuffer);
		GL20.glUniformMatrix4(this.projectionMatrixLocation, false, this.matrixBuffer);
		this.matrixBuffer.flip();
		
	}
	
	private void updateViewMatrix(final float eyeX, final float eyeY, final float eyeZ) {
		final Vector3f up = new Vector3f(0, 1, 0).normalise(null);
		
		final Vector3f eye = new Vector3f(eyeX, eyeY, eyeZ);
		final Vector3f center = new Vector3f(0, 0, 0);
		
		final Vector3f front = Vector3f.sub(center, eye, null).normalise(null); // front
		final Vector3f side = Vector3f.cross(front, up, null).normalise(null); // side
		final Vector3f upCam = Vector3f.cross(side, front, null).normalise(null); // up in cam
		
		this.viewMatrix = new Matrix4f();
		this.viewMatrix.m00 = side.x;
		this.viewMatrix.m01 = upCam.x;
		this.viewMatrix.m02 = -front.x;
		this.viewMatrix.m03 = 0;
		this.viewMatrix.m10 = side.y;
		this.viewMatrix.m11 = upCam.y;
		this.viewMatrix.m12 = -front.y;
		this.viewMatrix.m13 = 0;
		this.viewMatrix.m20 = side.z;
		this.viewMatrix.m21 = upCam.z;
		this.viewMatrix.m22 = -front.z;
		this.viewMatrix.m23 = 0;
		this.viewMatrix.m30 = Vector3f.dot(side, eye);
		this.viewMatrix.m31 = Vector3f.dot(upCam, eye);
		this.viewMatrix.m32 = Vector3f.dot(front, eye);
		this.viewMatrix.m33 = 1;
		
		this.viewMatrix = new Matrix4f();
		
		this.viewMatrix.store(this.matrixBuffer);
		GL20.glUniformMatrix4(this.viewMatrixLocation, false, this.matrixBuffer);
		this.matrixBuffer.flip();
	}
	
	private void updateModelMatrix() {
		this.modelMatrix = new Matrix4f();
		Matrix4f.translate(new Vector3f(0, 1, 0), this.modelMatrix, this.modelMatrix);
		
		this.modelMatrix.store(this.matrixBuffer);
		GL20.glUniformMatrix4(this.modelMatrixLocation, false, this.matrixBuffer);
		this.matrixBuffer.flip();
	}
	
	public void glDisplay() {
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		final float posX = (float) Math.sin(System.currentTimeMillis() / 2000.0) * 2;
		final float posY = (float) Math.sin(System.currentTimeMillis() / 1000.0) * 1;
		final float posZ = (float) Math.cos(System.currentTimeMillis() / 2000.0) * 2;
		
		this.mvp.glBind();
		this.program.glBind();
		
		this.updateProjectionMatrix();
		this.updateViewMatrix(posX, posY, posZ);
		this.updateModelMatrix();
		
		this.vao.glDraw();
		
		if (GL11.glGetError() != GL11.GL_NO_ERROR) {
			System.out.println(GLU.gluErrorString(GL11.glGetError()));
		}
	}
	
	public void glDispose() {
		
		this.program.glDispose();
		this.fragmentShader.glDispose();
		this.vertexShader.glDispose();
		
		this.vao.glDispose();
		this.vbo.glDispose();
		this.indicesBufferObject.glDispose();
		
		this.mvp.glDispose();
		
	}
	
}
