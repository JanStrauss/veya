package veya;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

public class Veya {
	public static void main(final String[] args) throws Exception {
		final Veya veya = new Veya();
		veya.run();
	}
	
	private int vsId;
	private int fsId;
	private int pId;
	private int vaoId;
	private int vboId;
	private int vboiId;
	private int indicesCount;
	
	public void run() throws Exception {
		try {
			Display.setDisplayMode(new DisplayMode(1280, 720));
			Display.setTitle("Veya");
			final PixelFormat pixelFormat = new PixelFormat();
			final ContextAttribs contextAtrributes = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
			
			Display.create(pixelFormat, contextAtrributes);
			
		} catch (final LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		this.setupOpenGL();
		this.setupShaders();
		this.setupTri();
		
		long last = System.currentTimeMillis();
		long counter = 0;
		
		while (!Display.isCloseRequested()) {
			
			this.handleControlls();
			
			this.render();
			
			Display.update();
			
			final long current = System.currentTimeMillis();
			if (current - last < 1000) {
				counter++;
			} else {
				System.out.println("fps=" + counter);
				counter = 0;
				last = current;
			}
		}
		
		Display.destroy();
	}
	
	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL20.glUseProgram(this.pId);
		
		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(this.vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		// Bind to the index VBO that has all the information about the order of
		// the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.vboiId);
		
		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLES, this.indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
		
		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
	}
	
	private void setupTri() {
		final Vertex v0 = new Vertex(0f, 0f, 0f, 1, 0, 0);
		final Vertex v1 = new Vertex(0.5f, 0f, 0f, 0, 1, 0);
		final Vertex v2 = new Vertex(0f, 0.5f, 0f, 0, 0, 1);
		final Vertex v3 = new Vertex(-0.5f, 0f, 0f, 0, 1, 0);
		final Vertex v4 = new Vertex(0f, -0.5f, 0f, 0, 0, 1);
		
		final Vertex[] vertices = new Vertex[] { v0, v1, v2, v3, v4 };
		
		final FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.elementCount);
		for (final Vertex vertex : vertices) {
			verticesBuffer.put(vertex.getXYZW());
			verticesBuffer.put(vertex.getRGBA());
		}
		verticesBuffer.flip();
		
		final byte[] indices = { 0, 1, 2, 0, 3, 4, 0, 1, 4, 0, 2, 3 };
		this.indicesCount = indices.length;
		final ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		
		this.vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(this.vaoId);
		
		// Create a new Vertex Buffer Object in memory and select it (bind)
		this.vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		// Put the positions in attribute list 0
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes, 0);
		// Put the colors in attribute list 1
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, Vertex.sizeInBytes, Vertex.elementBytes * 4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind) - INDICES
		this.vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private void setupShaders() throws Exception {
		int errorCheckValue = GL11.glGetError();
		
		// Load the vertex shader
		this.vsId = this.loadShader("src/main/shader/vertex.glsl", GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		this.fsId = this.loadShader("src/main/shader/fragment.glsl", GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		this.pId = GL20.glCreateProgram();
		GL20.glAttachShader(this.pId, this.vsId);
		GL20.glAttachShader(this.pId, this.fsId);
		
		// Position information will be attribute 0
		GL20.glBindAttribLocation(this.pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(this.pId, 1, "in_Color");
		// Texture information will be attribute 2
		GL20.glBindAttribLocation(this.pId, 2, "in_TextureCoord");
		
		GL20.glLinkProgram(this.pId);
		GL20.glValidateProgram(this.pId);
		
		errorCheckValue = GL11.glGetError();
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			System.out.println("ERROR - Could not create the shaders:" + GLU.gluErrorString(errorCheckValue));
			System.exit(-1);
		}
	}
	
	private int loadShader(final String filename, final int shaderType) throws Exception {
		int shader = 0;
		final StringBuilder shaderSource = new StringBuilder();
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
			
			if (shader == 0) {
				return 0;
			}
			
			try {
				final BufferedReader reader = new BufferedReader(new FileReader(filename));
				String line;
				while ((line = reader.readLine()) != null) {
					shaderSource.append(line).append("\n");
				}
				reader.close();
			} catch (final IOException e) {
				System.err.println("Could not read file.");
				e.printStackTrace();
				System.exit(-1);
			}
			
			ARBShaderObjects.glShaderSourceARB(shader, shaderSource.toString());
			ARBShaderObjects.glCompileShaderARB(shader);
			
			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
				throw new RuntimeException("Error creating shader: " + Veya.getLogInfo(shader));
			}
			
			return shader;
		} catch (final Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}
	
	private static String getLogInfo(final int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}
	
	private void setupOpenGL() {
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		GL11.glViewport(0, 0, 1280, 720);
		GL11.glClearColor(0, 0, 0, 0);
	}
	
	private void handleControlls() {
		while (Mouse.next()) {
			if (Mouse.getEventButton() == -1 || !Mouse.getEventButtonState()) {
				continue;
			}
			
			final int eventButton = Mouse.getEventButton();
			final int eventX = Mouse.getEventX();
			final int eventY = Mouse.getEventY();
			
			System.out.println("MOUSE EVENT: key=" + eventButton + ", x=" + eventX + ", y=" + eventY);
		}
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_W) {
					System.out.println("W");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_A) {
					System.out.println("A");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
					System.out.println("S");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
					System.out.println("D");
				}
			}
		}
		
	}
}
