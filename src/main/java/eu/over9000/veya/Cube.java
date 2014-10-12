package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.Util;

public class Cube {
	
	private final IntBuffer ibo_buffer;
	private final FloatBuffer vbo_buffer;
	
	private final FloatBuffer modelPositionBuffer;
	// private final IntBuffer textureIndexBuffer;
	
	int vbo_handle;
	int vao_handle;
	int ibo_handle;
	
	int texture_handle;
	
	int modPos_handle;
	// int texTable_handle;
	
	//@formatter:off
    private static final int[] indexData = new int[] {

        0, 1, 2, 3, Veya.RESTART,
        4, 5, 6, 7, Veya.RESTART,
        8, 9, 10, 11, Veya.RESTART,
        12, 13, 14, 15, Veya.RESTART,
        16, 17, 18, 19, Veya.RESTART,
        20, 21, 22, 23

    };
    
    // x, y, z,		r, g, b,	,s, t,		v,		nx, ny, nz
    private static final Vertex[] vertexData = new Vertex[] {

    	// BOTTOM
       new Vertex( 1.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 0.0f, 	1.0f, 1.0f,	0,		0,-1,0),
       new Vertex( 1.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 0.0f, 	1.0f, 0.0f,	0,		0,-1,0),
       new Vertex( 0.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 0.0f, 	0.0f, 1.0f,	0,		0,-1,0),
       new Vertex( 0.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 0.0f, 	0.0f, 0.0f,	0,		0,-1,0),

       // TOP
       new Vertex( 0.0f, 1.0f, 1.0f, 	1.0f, 1.0f, 0.0f, 	0.0f, 1.0f,	1,		0,1,0),
       new Vertex( 1.0f, 1.0f, 1.0f, 	1.0f, 1.0f, 0.0f, 	1.0f, 1.0f,	1,		0,1,0),
       new Vertex( 0.0f, 1.0f, 0.0f, 	1.0f, 1.0f, 0.0f, 	0.0f, 0.0f,	1,		0,1,0),
       new Vertex( 1.0f, 1.0f, 0.0f, 	1.0f, 1.0f, 0.0f, 	1.0f, 0.0f,	1,		0,1,0),
       
       // SOUTH
       new Vertex( 1.0f, 0.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	1.0f, 1.0f,	4,		0,0,1),
       new Vertex( 1.0f, 1.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	1.0f, 0.0f,	4,		0,0,1),
       new Vertex( 0.0f, 0.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.0f, 1.0f,	4,		0,0,1),
       new Vertex( 0.0f, 1.0f, 1.0f, 	0.0f, 1.0f, 0.0f, 	0.0f, 0.0f,	4,		0,0,1),

       // NORTH
       new Vertex( 0.0f, 1.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	1.0f, 0.0f,	2,		0,0,-1),
       new Vertex( 1.0f, 1.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	0.0f, 0.0f,	2,		0,0,-1),
       new Vertex( 0.0f, 0.0f, 0.0f, 	0.0f, 1.0f, 1.0f, 	1.0f, 1.0f,	2,		0,0,-1),
       new Vertex( 1.0f, 0.0f, 0.0f,   	0.0f, 1.0f, 1.0f, 	0.0f, 1.0f,	2,		0,0,-1),

       // WEST
       new Vertex( 0.0f, 0.0f, 1.0f, 	0.0f, 0.0f, 1.0f, 	1.0f, 1.0f,	5,		-1,0,0),
       new Vertex( 0.0f, 1.0f, 1.0f, 	0.0f, 0.0f, 1.0f, 	1.0f, 0.0f,	5,		-1,0,0),
       new Vertex( 0.0f, 0.0f, 0.0f, 	0.0f, 0.0f, 1.0f, 	0.0f, 1.0f,	5,		-1,0,0),
       new Vertex( 0.0f, 1.0f, 0.0f, 	0.0f, 0.0f, 1.0f, 	0.0f, 0.0f,	5,		-1,0,0),
       
       // EAST
       new Vertex( 1.0f, 1.0f, 0.0f, 	1.0f, 0.0f, 1.0f, 	1.0f, 0.0f,	3,		1,0,0),
       new Vertex( 1.0f, 1.0f, 1.0f, 	1.0f, 0.0f, 1.0f, 	0.0f, 0.0f,	3,		1,0,0),
       new Vertex( 1.0f, 0.0f, 0.0f, 	1.0f, 0.0f, 1.0f, 	1.0f, 1.0f,	3,		1,0,0),
       new Vertex( 1.0f, 0.0f, 1.0f, 	1.0f, 0.0f, 1.0f, 	0.0f, 1.0f,	3,		1,0,0)

    };
	
    //@formatter:on
	private final Program program;
	
	public Cube(final Program program) {
		this.texture_handle = TextureUtil.loadPNGTexture("BLOCKS", Cube.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);
		
		this.program = program;
		
		// Fill buffer
		
		this.ibo_buffer = BufferUtils.createIntBuffer(Cube.indexData.length);
		this.ibo_buffer.put(Cube.indexData);
		this.ibo_buffer.flip();
		
		this.vbo_buffer = BufferUtils.createFloatBuffer(Cube.vertexData.length * Vertex.elementCount);
		for (final Vertex vertex : Cube.vertexData) {
			this.vbo_buffer.put(vertex.getElements());
		}
		this.vbo_buffer.flip();
		
		this.modelPositionBuffer = BufferUtils.createFloatBuffer(4 * Scene.CHUNK_BLOCK_COUNT);
		// this.textureIndexBuffer = BufferUtils.createIntBuffer(6 * Scene.CHUNK_BLOCK_COUNT);
		
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
		
		this.modPos_handle = GL15.glGenBuffers();
		// this.texTable_handle = GL15.glGenBuffers();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle);
		GL20.glEnableVertexAttribArray(program.getAttribLocation("vertexPosition"));
		GL20.glVertexAttribPointer(program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);
		
		GL20.glEnableVertexAttribArray(program.getAttribLocation("vertexColor"));
		GL20.glVertexAttribPointer(program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);
		
		GL20.glEnableVertexAttribArray(program.getAttribLocation("vertexTexturePosition"));
		GL20.glVertexAttribPointer(program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);
		
		GL20.glEnableVertexAttribArray(program.getAttribLocation("vertexNormal"));
		GL20.glVertexAttribPointer(program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle);
		
		GL30.glBindVertexArray(0);
	}
	
	private void updateInstancedBuffers(final List<CubeInstance> instances) {
		
		for (final CubeInstance cubeInstance : instances) {
			cubeInstance.getModelPosition().store(this.modelPositionBuffer);
			// this.textureIndexBuffer.put(cubeInstance.getBlockType().getTextureLookupArray());
		}
		
		this.modelPositionBuffer.flip();
		// this.textureIndexBuffer.flip();
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.modPos_handle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.modelPositionBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		org.lwjgl.opengl.Util.checkGLError();
		
		// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.texTable_handle);
		// GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.textureIndexBuffer, GL15.GL_STATIC_DRAW);
		// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		//
		// org.lwjgl.opengl.Util.checkGLError();
		
		GL30.glBindVertexArray(this.vao_handle);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.modPos_handle);
		GL20.glEnableVertexAttribArray(this.program.getAttribLocation("instancedPosition"));
		GL20.glVertexAttribPointer(this.program.getAttribLocation("instancedPosition"), 4, GL11.GL_FLOAT, false, 4 * 4, 0);
		GL33.glVertexAttribDivisor(this.program.getAttribLocation("instancedPosition"), 1);
		
		org.lwjgl.opengl.Util.checkGLError();
		
		// GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.texTable_handle);
		// org.lwjgl.opengl.Util.checkGLError();
		// GL20.glEnableVertexAttribArray(this.program.getAttribLocation("instancedTextureTable"));
		// org.lwjgl.opengl.Util.checkGLError();
		// GL20.glVertexAttribPointer(this.program.getAttribLocation("instancedTextureTable"), 1, GL11.GL_INT, false, 1 * 4, 0);
		// org.lwjgl.opengl.Util.checkGLError();
		// GL33.glVertexAttribDivisor(this.program.getAttribLocation("instancedTextureTable"), 1);
		
		GL30.glBindVertexArray(0);
	}
	
	void dispose() {
		GL11.glDeleteTextures(this.texture_handle);
		
		GL30.glDeleteVertexArrays(this.vao_handle);
		GL15.glDeleteBuffers(this.ibo_handle);
		GL15.glDeleteBuffers(this.vbo_handle);
		// GL15.glDeleteBuffers(this.texTable_handle);
		GL15.glDeleteBuffers(this.modPos_handle);
		
		this.vao_handle = -1;
		this.vbo_handle = -1;
		this.ibo_handle = -1;
		// this.texTable_handle = -1;
		this.modPos_handle = -1;
	}
	
	void initInstanced(final List<CubeInstance> instances) {
		this.updateInstancedBuffers(instances);
	}
	
	void renderInstanced(final List<CubeInstance> instances) {
		
		org.lwjgl.opengl.Util.checkGLError();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Util.checkGLError();
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		Util.checkGLError();
		GL30.glBindVertexArray(this.vao_handle);
		Util.checkGLError();
		this.program.enableVAttributes();
		Util.checkGLError();
		GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, Cube.indexData.length, GL11.GL_UNSIGNED_INT, 0, instances.size());
		Util.checkGLError();
		
		this.program.disableVAttributes();
		GL30.glBindVertexArray(0);
	}
	
	void render() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		
		GL30.glBindVertexArray(this.vao_handle);
		this.program.enableVAttributes();
		
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, Cube.indexData.length, GL11.GL_UNSIGNED_INT, 0);
		
		this.program.disableVAttributes();
		GL30.glBindVertexArray(0);
		
	}
	
}