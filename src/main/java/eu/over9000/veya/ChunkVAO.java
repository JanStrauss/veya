package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.google.common.primitives.Ints;

import eu.over9000.veya.data.Block;
import eu.over9000.veya.data.Chunk;

public class ChunkVAO {
	private final IntBuffer ibo_buffer;
	private final FloatBuffer vbo_buffer;
	int vbo_handle;
	int vao_handle;
	int ibo_handle;
	
	private final int[] indexData;
	private final Vertex[] vertexData;
	
	public ChunkVAO(final Chunk chunk, final Program program) {
		final List<Integer> indexDataList = new ArrayList<>();
		final List<Vertex> vertexDataList = new ArrayList<>();
		
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
				for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
					final Block block = chunk.getBlockAt(x, y, z);
					
					if (block.getNeighborBottom() == null) {
						ChunkVAO.addBottomOfBlock(indexDataList, vertexDataList, block);
					}
					
					if (block.getNeighborTop() == null) {
						ChunkVAO.addTopOfBlock(indexDataList, vertexDataList, block);
					}
					
					if (block.getNeighborNorth() == null) {
						ChunkVAO.addNorthOfBlock(indexDataList, vertexDataList, block);
					}
					
					if (block.getNeighborSouth() == null) {
						ChunkVAO.addSouthOfBlock(indexDataList, vertexDataList, block);
					}
					
					if (block.getNeighborWest() == null) {
						ChunkVAO.addWestOfBlock(indexDataList, vertexDataList, block);
					}
					
					if (block.getNeighborEast() == null) {
						ChunkVAO.addEastOfBlock(indexDataList, vertexDataList, block);
					}
					
				}
			}
		}
		
		this.indexData = Ints.toArray(indexDataList);
		this.vertexData = vertexDataList.toArray(new Vertex[0]);
		
		this.ibo_buffer = BufferUtils.createIntBuffer(this.indexData.length);
		this.ibo_buffer.put(this.indexData);
		this.ibo_buffer.flip();
		
		this.vbo_buffer = BufferUtils.createFloatBuffer(this.vertexData.length * Vertex.elementCount);
		for (final Vertex vertex : this.vertexData) {
			this.vbo_buffer.put(vertex.getElements());
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
	
	private static void addBottomOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getType().getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getType().getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getType().getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getType().getTextureIDBottom(), 0, -1, 0));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
	
	private static void addTopOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getType().getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getType().getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getType().getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getType().getTextureIDTop(), 0, 1, 0));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
	
	private static void addSouthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getType().getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getType().getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getType().getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getType().getTextureIDSouth(), 0, 0, 1));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
	
	private static void addNorthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getType().getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getType().getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getType().getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getType().getTextureIDNorth(), 0, 0, -1));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
	
	private static void addWestOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getType().getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getType().getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getType().getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getType().getTextureIDWest(), -1, 0, 0));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
	
	private static void addEastOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final Block block) {
		final int firstIndex = vertexDataList.size();
		
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getType().getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 1.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getType().getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 0.0f + block.getZ(), 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getType().getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + block.getX(), 0.0f + block.getY(), 1.0f + block.getZ(), 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getType().getTextureIDEast(), 1, 0, 0));
		
		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		
		indexDataList.add(Veya.RESTART);
	}
}
