package veya;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TheQuadExampleMoving {
	// Entry point for the application
	public static void main(final String[] args) {
		new TheQuadExampleMoving();
	}
	
	// Setup variables
	private final String WINDOW_TITLE = "The Quad: Moving";
	private final int WIDTH = 1280;
	private final int HEIGHT = 720;
	private final double PI = 3.14159265358979323846;
	// Quad variables
	private int vaoId = 0;
	private int vboId = 0;
	private int vboiId = 0;
	private int indicesCount = 0;
	private VertexData[] vertices = null;
	private ByteBuffer verticesByteBuffer = null;
	// Shader variables
	private int pId = 0;
	// Texture variables
	private final int[] texIds = new int[] { 0, 0 };
	private int textureSelector = 0;
	// Moving variables
	private int projectionMatrixLocation = 0;
	private int viewMatrixLocation = 0;
	private int modelMatrixLocation = 0;
	private Matrix4f projectionMatrix = null;
	private Matrix4f viewMatrix = null;
	private Matrix4f modelMatrix = null;
	private Vector3f modelPos = null;
	private Vector3f modelAngle = null;
	private Vector3f modelScale = null;
	private Vector3f cameraPos = null;
	private FloatBuffer matrix44Buffer = null;
	private Vector3f cameraTarget;
	
	public TheQuadExampleMoving() {
		// Initialize OpenGL (Display)
		this.setupOpenGL();
		
		this.setupQuad();
		this.setupShaders();
		this.setupTextures();
		this.setupMatrices();
		
		while (!Display.isCloseRequested()) {
			// Do a single loop (logic/render)
			this.loopCycle();
			
			// Force a maximum FPS of about 60
			Display.sync(60);
			// Let the CPU synchronize with the GPU if GPU is tagging behind
			Display.update();
		}
		
		// Destroy OpenGL (Display)
		this.destroyOpenGL();
	}
	
	private void setupMatrices() {
		// Setup projection matrix
		this.projectionMatrix = new Matrix4f();
		final float fieldOfView = 60f;
		final float aspectRatio = (float) this.WIDTH / (float) this.HEIGHT;
		final float near_plane = 0.1f;
		final float far_plane = 100f;
		
		final float y_scale = this.coTangent(this.degreesToRadians(fieldOfView / 2f));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = far_plane - near_plane;
		this.projectionMatrix.m00 = x_scale;
		this.projectionMatrix.m11 = y_scale;
		this.projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		this.projectionMatrix.m23 = -1;
		this.projectionMatrix.m32 = -(2 * near_plane * far_plane / frustum_length);
		this.projectionMatrix.m33 = 0;
		
		// Setup view matrix
		this.viewMatrix = new Matrix4f();
		Matrix4f.translate(new Vector3f(5, 5, 0), this.viewMatrix, this.viewMatrix);
		
		// Setup model matrix
		this.modelMatrix = new Matrix4f();
		
		// Create a FloatBuffer with the proper size to store our matrices later
		this.matrix44Buffer = BufferUtils.createFloatBuffer(16);
	}
	
	private void setupTextures() {
		this.texIds[0] = this.loadPNGTexture("src/main/resources/test_rock.png", GL13.GL_TEXTURE0);
		this.texIds[1] = this.loadPNGTexture("src/main/resources/test_tex.png", GL13.GL_TEXTURE0);
		
		this.exitOnGLError("setupTexture");
	}
	
	private void setupOpenGL() {
		// Setup an OpenGL context with API version 3.2
		try {
			final PixelFormat pixelFormat = new PixelFormat();
			final ContextAttribs contextAtrributes = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
			
			Display.setDisplayMode(new DisplayMode(this.WIDTH, this.HEIGHT));
			Display.setTitle(this.WINDOW_TITLE);
			Display.create(pixelFormat, contextAtrributes);
			
			GL11.glViewport(0, 0, this.WIDTH, this.HEIGHT);
		} catch (final LWJGLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Setup an XNA like background color
		GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);
		
		// Map the internal OpenGL coordinate system to the entire screen
		GL11.glViewport(0, 0, this.WIDTH, this.HEIGHT);
		
		this.exitOnGLError("setupOpenGL");
	}
	
	private void setupQuad() {
		// We'll define our quad using 4 vertices of the custom 'TexturedVertex' class
		final VertexData v0 = new VertexData();
		v0.setXYZ(-0.5f, 0.5f, 0);
		v0.setRGB(1, 0, 0);
		v0.setST(0, 0);
		final VertexData v1 = new VertexData();
		v1.setXYZ(-0.5f, -0.5f, 0);
		v1.setRGB(0, 1, 0);
		v1.setST(0, 1);
		final VertexData v2 = new VertexData();
		v2.setXYZ(0.5f, -0.5f, 0);
		v2.setRGB(0, 0, 1);
		v2.setST(1, 1);
		final VertexData v3 = new VertexData();
		v3.setXYZ(0.5f, 0.5f, 0);
		v3.setRGB(1, 1, 1);
		v3.setST(1, 0);
		
		this.vertices = new VertexData[] { v0, v1, v2, v3 };
		
		// Put each 'Vertex' in one FloatBuffer
		this.verticesByteBuffer = BufferUtils.createByteBuffer(this.vertices.length * VertexData.stride);
		final FloatBuffer verticesFloatBuffer = this.verticesByteBuffer.asFloatBuffer();
		for (final VertexData vertice : this.vertices) {
			// Add position, color and texture floats to the buffer
			verticesFloatBuffer.put(vertice.getElements());
		}
		verticesFloatBuffer.flip();
		
		// OpenGL expects to draw vertices in counter clockwise order by default
		final byte[] indices = { 0, 1, 2, 2, 3, 0 };
		this.indicesCount = indices.length;
		final ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(this.indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		
		// Create a new Vertex Array Object in memory and select it (bind)
		this.vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(this.vaoId);
		
		// Create a new Vertex Buffer Object in memory and select it (bind)
		this.vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesFloatBuffer, GL15.GL_STREAM_DRAW);
		
		// Put the position coordinates in attribute list 0
		GL20.glVertexAttribPointer(0, VertexData.positionElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.positionByteOffset);
		// Put the color components in attribute list 1
		GL20.glVertexAttribPointer(1, VertexData.colorElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.colorByteOffset);
		// Put the texture coordinates in attribute list 2
		GL20.glVertexAttribPointer(2, VertexData.textureElementCount, GL11.GL_FLOAT, false, VertexData.stride, VertexData.textureByteOffset);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
		
		// Create a new VBO for the indices and select it (bind) - INDICES
		this.vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		// Set the default quad rotation, scale and position values
		this.modelPos = new Vector3f(0, 0, 0);
		this.modelAngle = new Vector3f(0, 0, 0);
		this.modelScale = new Vector3f(1, 1, 1);
		this.cameraPos = new Vector3f(2, 2, -5);
		this.cameraTarget = new Vector3f(0, 0, 0);
		
		this.exitOnGLError("setupQuad");
	}
	
	private void setupShaders() {
		// Load the vertex shader
		final int vsId = this.loadShader("src/main/shader/vertex.glsl", GL20.GL_VERTEX_SHADER);
		// Load the fragment shader
		final int fsId = this.loadShader("src/main/shader/fragment.glsl", GL20.GL_FRAGMENT_SHADER);
		
		// Create a new shader program that links both shaders
		this.pId = GL20.glCreateProgram();
		GL20.glAttachShader(this.pId, vsId);
		GL20.glAttachShader(this.pId, fsId);
		
		// Position information will be attribute 0
		GL20.glBindAttribLocation(this.pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(this.pId, 1, "in_Color");
		// Textute information will be attribute 2
		GL20.glBindAttribLocation(this.pId, 2, "in_TextureCoord");
		
		GL20.glLinkProgram(this.pId);
		GL20.glValidateProgram(this.pId);
		
		// Get matrices uniform locations
		this.projectionMatrixLocation = GL20.glGetUniformLocation(this.pId, "projectionMatrix");
		this.viewMatrixLocation = GL20.glGetUniformLocation(this.pId, "viewMatrix");
		this.modelMatrixLocation = GL20.glGetUniformLocation(this.pId, "modelMatrix");
		
		this.exitOnGLError("setupShaders");
	}
	
	private void logicCycle() {
		// -- Input processing
		final float rotationDelta = 15f;
		final float scaleDelta = 0.1f;
		final float posDelta = 0.1f;
		final Vector3f scaleAddResolution = new Vector3f(scaleDelta, scaleDelta, scaleDelta);
		final Vector3f scaleMinusResolution = new Vector3f(-scaleDelta, -scaleDelta, -scaleDelta);
		
		while (Keyboard.next()) {
			// Only listen to events where the key was pressed (down event)
			if (!Keyboard.getEventKeyState()) {
				continue;
			}
			
			// Switch textures depending on the key released
			switch (Keyboard.getEventKey()) {
				case Keyboard.KEY_1:
					this.textureSelector = 0;
					break;
				case Keyboard.KEY_2:
					this.textureSelector = 1;
					break;
			}
			
			// Change model scale, rotation and translation values
			switch (Keyboard.getEventKey()) {
			// Move
				case Keyboard.KEY_UP:
					this.modelPos.y += posDelta;
					break;
				case Keyboard.KEY_DOWN:
					this.modelPos.y -= posDelta;
					break;
				// Scale
				case Keyboard.KEY_P:
					Vector3f.add(this.modelScale, scaleAddResolution, this.modelScale);
					break;
				case Keyboard.KEY_M:
					Vector3f.add(this.modelScale, scaleMinusResolution, this.modelScale);
					break;
				// Rotation
				case Keyboard.KEY_LEFT:
					this.modelAngle.z += rotationDelta;
					break;
				case Keyboard.KEY_RIGHT:
					this.modelAngle.z -= rotationDelta;
					break;
			}
		}
		
		// -- Update matrices
		// Reset view and model matrices
		this.viewMatrix = new Matrix4f();
		this.modelMatrix = new Matrix4f();
		
		// Translate camera
		Matrix4f.translate(this.cameraPos, this.viewMatrix, this.viewMatrix);
		
		// Scale, translate and rotate model
		Matrix4f.scale(this.modelScale, this.modelMatrix, this.modelMatrix);
		Matrix4f.translate(this.modelPos, this.modelMatrix, this.modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(this.modelAngle.z), new Vector3f(0, 0, 1), this.modelMatrix, this.modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(this.modelAngle.y), new Vector3f(0, 1, 0), this.modelMatrix, this.modelMatrix);
		Matrix4f.rotate(this.degreesToRadians(this.modelAngle.x), new Vector3f(1, 0, 0), this.modelMatrix, this.modelMatrix);
		
		// Upload matrices to the uniform variables
		GL20.glUseProgram(this.pId);
		
		this.projectionMatrix.store(this.matrix44Buffer);
		this.matrix44Buffer.flip();
		GL20.glUniformMatrix4(this.projectionMatrixLocation, false, this.matrix44Buffer);
		this.viewMatrix.store(this.matrix44Buffer);
		this.matrix44Buffer.flip();
		GL20.glUniformMatrix4(this.viewMatrixLocation, false, this.matrix44Buffer);
		this.modelMatrix.store(this.matrix44Buffer);
		this.matrix44Buffer.flip();
		GL20.glUniformMatrix4(this.modelMatrixLocation, false, this.matrix44Buffer);
		
		GL20.glUseProgram(0);
		
		this.exitOnGLError("logicCycle");
	}
	
	private void renderCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL20.glUseProgram(this.pId);
		
		// Bind the texture
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texIds[this.textureSelector]);
		
		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(this.vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		// Bind to the index VBO that has all the information about the order of the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.vboiId);
		
		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLES, this.indicesCount, GL11.GL_UNSIGNED_BYTE, 0);
		
		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		
		GL20.glUseProgram(0);
		
		this.exitOnGLError("renderCycle");
	}
	
	private void loopCycle() {
		// Update logic
		this.logicCycle();
		// Update rendered frame
		this.renderCycle();
		
		this.exitOnGLError("loopCycle");
	}
	
	private void destroyOpenGL() {
		// Delete the texture
		GL11.glDeleteTextures(this.texIds[0]);
		GL11.glDeleteTextures(this.texIds[1]);
		
		// Delete the shaders
		GL20.glUseProgram(0);
		GL20.glDeleteProgram(this.pId);
		
		// Select the VAO
		GL30.glBindVertexArray(this.vaoId);
		
		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		
		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(this.vboId);
		
		// Delete the index VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(this.vboiId);
		
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(this.vaoId);
		
		this.exitOnGLError("destroyOpenGL");
		
		Display.destroy();
	}
	
	private int loadShader(final String filename, final int type) {
		final StringBuilder shaderSource = new StringBuilder();
		int shaderID = 0;
		
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
		
		shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		
		if (GL20.glGetShader(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Could not compile shader.");
			System.exit(-1);
		}
		
		this.exitOnGLError("loadShader");
		
		return shaderID;
	}
	
	private int loadPNGTexture(final String filename, final int textureUnit) {
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;
		
		try {
			// Open the PNG file as an InputStream
			final InputStream in = new FileInputStream(filename);
			// Link the PNG decoder to this stream
			final PNGDecoder decoder = new PNGDecoder(in);
			
			// Get the width and height of the texture
			tWidth = decoder.getWidth();
			tHeight = decoder.getHeight();
			
			// Decode the PNG file in a ByteBuffer
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Create a new texture object in memory and bind it
		final int texId = GL11.glGenTextures();
		GL13.glActiveTexture(textureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		
		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		
		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		
		this.exitOnGLError("loadPNGTexture");
		
		return texId;
	}
	
	private float coTangent(final float angle) {
		return (float) (1f / Math.tan(angle));
	}
	
	private float degreesToRadians(final float degrees) {
		return degrees * (float) (this.PI / 180d);
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
}
