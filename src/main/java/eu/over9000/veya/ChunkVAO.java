package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.google.common.primitives.Ints;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;

public class ChunkVAO {

	private int vbo_handle;
	private int vao_handle;
	private int ibo_handle;

	private final int indexData_length;

	private final IntBuffer ibo_buffer;
	private final FloatBuffer vbo_buffer;

	private final Program program;

	public ChunkVAO(final Chunk chunk, final Program program) {
		this.program = program;

		final int[] indexData;
		final Vertex[] vertexData;

		final List<Integer> indexDataList = new ArrayList<>();
		final List<Vertex> vertexDataList = new ArrayList<>();


		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
				for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
					final BlockType block = chunk.getBlockAt(x, y, z);

					if (block != null) {
						final int worldX = chunk.getChunkX() * Chunk.CHUNK_SIZE + x;
						final int worldY = chunk.getChunkY() * Chunk.CHUNK_SIZE + y;
						final int worldZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE + z;

						if (chunk.getNeighborBlockBottom(x, y, z) == null) {
							ChunkVAO.addBottomOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}

						if (chunk.getNeighborBlockTop(x, y, z) == null) {
							ChunkVAO.addTopOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}

						if (chunk.getNeighborBlockNorth(x, y, z) == null) {
							ChunkVAO.addNorthOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}

						if (chunk.getNeighborBlockSouth(x, y, z) == null) {
							ChunkVAO.addSouthOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}

						if (chunk.getNeighborBlockWest(x, y, z) == null) {
							ChunkVAO.addWestOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}

						if (chunk.getNeighborBlockEast(x, y, z) == null) {
							ChunkVAO.addEastOfBlock(indexDataList, vertexDataList, block, worldX, worldY, worldZ);
						}
					}

				}
			}
		}

		indexData = Ints.toArray(indexDataList);
		vertexData = vertexDataList.toArray(new Vertex[0]);
		this.indexData_length = indexData.length;

		this.ibo_buffer = BufferUtils.createIntBuffer(indexData.length);
		this.ibo_buffer.put(indexData);
		this.ibo_buffer.flip();

		this.vbo_buffer = BufferUtils.createFloatBuffer(vertexData.length * Vertex.elementCount);
		for (final Vertex vertex : vertexData) {
			this.vbo_buffer.put(vertex.getElements());
		}
		this.vbo_buffer.flip();
	}

	public void create() {
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
		GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexPosition"));
		GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);

		GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexColor"));
		GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);

		GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexTexturePosition"));
		GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);

		GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexNormal"));
		GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle);

		GL30.glBindVertexArray(0);

		// System.out.println("created ChunkVAO with " + this.vertexData.length + " vertices");
	}

	public void render() {

		try {
			GL30.glBindVertexArray(this.vao_handle);
			GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.indexData_length, GL11.GL_UNSIGNED_INT, 0);
			GL30.glBindVertexArray(0);
		} catch (final OpenGLException e) {
			System.out.println("vao_handle: " + this.vao_handle);
			e.printStackTrace();
		}

	}

	public void dispose() {
		GL30.glDeleteVertexArrays(this.vao_handle);
		GL15.glDeleteBuffers(this.ibo_handle);
		GL15.glDeleteBuffers(this.vbo_handle);

		this.vao_handle = -1;
		this.vbo_handle = -1;
		this.ibo_handle = -1;
	}

	private static void addBottomOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDBottom(), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDBottom(), 0, -1, 0));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	private static void addTopOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();

		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDTop(), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDTop(), 0, 1, 0));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	private static void addSouthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();

		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDSouth(), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDSouth(), 0, 0, 1));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	private static void addNorthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();

		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDNorth(), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDNorth(), 0, 0, -1));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	private static void addWestOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();

		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDWest(), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDWest(), -1, 0, 0));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	private static void addEastOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final int firstIndex = vertexDataList.size();

		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDEast(), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDEast(), 1, 0, 0));

		indexDataList.add(firstIndex + 0);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);

		indexDataList.add(Veya.RESTART);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.ibo_handle;
		result = prime * result + this.vao_handle;
		result = prime * result + this.vbo_handle;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ChunkVAO)) {
			return false;
		}
		final ChunkVAO other = (ChunkVAO) obj;
		if (this.ibo_handle != other.ibo_handle) {
			return false;
		}
		if (this.vao_handle != other.vao_handle) {
			return false;
		}
		if (this.vbo_handle != other.vbo_handle) {
			return false;
		}
		return true;
	}

}
