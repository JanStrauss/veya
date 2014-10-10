package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Cube {
	
	private final IntBuffer ibo_buffer;
	private final FloatBuffer vbo_buffer;
	
	int vbo_handle;
	int vao_handle;
	int ibo_handle;
	
	int texture_handle;
	
	//@formatter:off
    private static final int[] indexData = new int[] {

        0, 1, 2, 3, Veya.RESTART,
        4, 5, 6, 7, Veya.RESTART,
        8, 9, 10, 11, Veya.RESTART,
        12, 13, 14, 15, Veya.RESTART,
        16, 17, 18, 19, Veya.RESTART,
        20, 21, 22, 23

    };
    
    // x, y, z,		r, g, b,	,s, t,		nx, ny, nz
    private static final Vertex[] vertexData = new Vertex[] {

       new Vertex( 1.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 0.0f, 	0.0f, 0.0f,		0,-1,0),
       new Vertex( 1.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 0.0f, 	0.0f, 1.0f,		0,-1,0),
       new Vertex( 0.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 0.0f, 	1.0f, 0.0f,		0,-1,0),
       new Vertex( 0.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 0.0f, 	1.0f, 1.0f,		0,-1,0),

       new Vertex( 0.0f, 1.0f, 1.0f, 	1.0f, 1.0f, 0.0f, 	0.0f, 0.0f,		0,1,0),
       new Vertex( 1.0f, 1.0f, 1.0f, 	1.0f, 1.0f, 0.0f, 	0.0f, 1.0f,		0,1,0),
       new Vertex( 0.0f, 1.0f, 0.0f, 	1.0f, 1.0f, 0.0f, 	1.0f, 0.0f,		0,1,0),
       new Vertex( 1.0f, 1.0f, 0.0f, 	1.0f, 1.0f, 0.0f, 	1.0f, 1.0f,		0,1,0),
       
       new Vertex( 1.0f, 0.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.5f, 0.249f,		0,0,1),
       new Vertex( 1.0f, 1.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.5f, 0.0f,			0,0,1),
       new Vertex( 0.0f, 0.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.749f, 0.249f,		0,0,1),
       new Vertex( 0.0f, 1.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.749f, 0.0f,		0,0,1),

       new Vertex( 0.0f, 1.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	0.5f, 0.0f,			0,0,-1),
       new Vertex( 1.0f, 1.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	0.749f, 0.0f,		0,0,-1),
       new Vertex( 0.0f, 0.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	0.5f, 0.249f,		0,0,-1),
       new Vertex( 1.0f, 0.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	0.749f, 0.249f,		0,0,-1),

       new Vertex( 0.0f, 0.0f, 1.0f, 	0.0f, 0.0f, 1.0f, 	0.5f, 0.249f,		-1,0,0),
       new Vertex( 0.0f, 1.0f, 1.0f, 	0.0f, 0.0f, 1.0f, 	0.5f, 0.0f,			-1,0,0),
       new Vertex( 0.0f, 0.0f, 0.0f, 	0.0f, 0.0f, 1.0f, 	0.749f, 0.249f,		-1,0,0),
       new Vertex( 0.0f, 1.0f, 0.0f, 	0.0f, 0.0f, 1.0f, 	0.749f, 0.0f,		-1,0,0),

       new Vertex( 1.0f, 1.0f, 0.0f, 	1.0f, 0.0f, 1.0f, 	0.5f, 0.0f,			1,0,0),
       new Vertex( 1.0f, 1.0f, 1.0f, 	1.0f, 0.0f, 1.0f, 	0.749f, 0.0f,		1,0,0),
       new Vertex( 1.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 1.0f, 	0.5f, 0.249f,		1,0,0),
       new Vertex( 1.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 1.0f, 	0.749f,0.249f,		1,0,0)

    };
	
    //@formatter:on
	Program shader;
	
	public Cube(final Program shader) {
		this.texture_handle = Util.loadPNGTexture("BLOCKS", Cube.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);
		
		this.shader = shader;
		
		// Fill buffer
		
		this.ibo_buffer = BufferUtils.createIntBuffer(Cube.indexData.length);
		this.ibo_buffer.put(Cube.indexData);
		this.ibo_buffer.flip();
		
		this.vbo_buffer = BufferUtils.createFloatBuffer(Cube.vertexData.length * Vertex.elementCount);
		for (final Vertex element : Cube.vertexData) {
			this.vbo_buffer.put(element.getElements());
		}
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
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexPosition"), 4, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);
		
		GL20.glEnableVertexAttribArray(shader.getAttribLocation("vertexColor"));
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexColor"), 4, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);
		
		GL20.glEnableVertexAttribArray(shader.getAttribLocation("vertexTexturePosition"));
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexTexturePosition"), 2, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);
		
		GL20.glEnableVertexAttribArray(shader.getAttribLocation("vertexNormal"));
		GL20.glVertexAttribPointer(shader.getAttribLocation("vertexNormal"), 3, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle);
		
		GL30.glBindVertexArray(0);
	}
	
	void dispose() {
		GL11.glDeleteTextures(this.texture_handle);
		
		GL30.glDeleteVertexArrays(this.vao_handle);
		GL15.glDeleteBuffers(this.ibo_handle);
		GL15.glDeleteBuffers(this.vbo_handle);
		
		this.vao_handle = -1;
		this.vbo_handle = -1;
		this.ibo_handle = -1;
	}
	
	void render() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		
		GL30.glBindVertexArray(this.vao_handle);
		this.shader.enableVAttributes();
		
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, Cube.indexData.length, GL11.GL_UNSIGNED_INT, 0);
		
		this.shader.disableVAttributes();
		GL30.glBindVertexArray(0);
		
	}
	
}