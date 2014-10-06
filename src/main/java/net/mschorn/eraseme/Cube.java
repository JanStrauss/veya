package net.mschorn.eraseme;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Cube {
	
	private final IntBuffer ibo_buffer;
	private final FloatBuffer vbo_buffer;
	
	int vbo_handle;
	int vao_handle;
	int ibo_handle;
	
	//@formatter:off
	 private static final int[] indexData = new int[] {
		 
		 0, 1, 2, 3, Test.RESTART,
		 5, 1, 4, 0, Test.RESTART,
		 0, 2, 4, 6, Test.RESTART,
		 4, 6, 5, 7, Test.RESTART,
		 1, 5, 3, 7, Test.RESTART,
		 2, 3, 6, 7
	};
		  
	// x, y, z, r, g, b
	private static final float[] vertexData = new float[] {
		 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
		 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
		 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
		 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
		 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
		 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
		 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
		 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
	};
	
	Shader shader;
	
	//@formatter:on
	
	public Cube(final Shader shader) {
		this.shader = shader;
		
		// Fill buffer
		
		this.ibo_buffer = BufferUtils.createIntBuffer(Cube.indexData.length);
		this.ibo_buffer.put(Cube.indexData);
		this.ibo_buffer.flip();
		
		this.vbo_buffer = BufferUtils.createFloatBuffer(Cube.vertexData.length);
		this.vbo_buffer.put(Cube.vertexData);
		this.vbo_buffer.flip();
		
		// create objects
		this.ibo_handle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_buffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		this.vbo_handle = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbo_buffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		this.vao_handle = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(this.vao_handle);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle);
		GL20.glEnableVertexAttribArray(shader.getAttribLocation("vertexPosition"));
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexPosition"), 3, GL11.GL_FLOAT, false, 6 * 4, 0 * 4);
		GL20.glEnableVertexAttribArray(shader.getAttribLocation("vertexColor"));
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexColor"), 3, GL11.GL_FLOAT, false, 6 * 4, 3 * 4);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle);
		
		GL30.glBindVertexArray(0);
	}
	
	void dispose() {
		GL30.glDeleteVertexArrays(this.vao_handle);
		GL15.glDeleteBuffers(this.ibo_handle);
		GL15.glDeleteBuffers(this.vbo_handle);
		
		this.vao_handle = -1;
		this.vbo_handle = -1;
		this.ibo_handle = -1;
	}
	
	void render() {
		
		GL30.glBindVertexArray(this.vao_handle);
		this.shader.enableVAttributes();
		
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, Cube.indexData.length, GL11.GL_UNSIGNED_INT, 0);
		
		this.shader.disableVAttributes();
		GL30.glBindVertexArray(0);
		
	}
	
}