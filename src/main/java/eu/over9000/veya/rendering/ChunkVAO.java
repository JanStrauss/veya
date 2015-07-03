package eu.over9000.veya.rendering;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.google.common.primitives.Ints;

import eu.over9000.veya.Veya;
import eu.over9000.veya.util.Side;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;

public class ChunkVAO {
	
	private final boolean holdsTransparent;
	private final boolean holdsSolid;
	
	private int vbo_handle_solid;
	private int vbo_handle_transparent;
	
	private int vao_handle_solid;
	private int vao_handle_transparent;
	
	private int ibo_handle_solid;
	private int ibo_handle_transparent;
	
	private int index_length_solid;
	private int index_length_transparent;
	
	private IntBuffer ibo_buffer_solid;
	private IntBuffer ibo_buffer_transparent;
	
	private FloatBuffer vbo_buffer_solid;
	private FloatBuffer vbo_buffer_transparent;
	
	public ChunkVAO(final Chunk chunk) {
		
		final int[] index_solid;
		final int[] index_transparent;
		
		final Vertex[] vertex_data_solid;
		final Vertex[] vertex_data_transparent;
		
		final List<Integer> indexListSolid = new ArrayList<>();
		final List<Integer> indexListTransparent = new ArrayList<>();
		
		final List<Vertex> vertexListSolid = new ArrayList<>();
		final List<Vertex> vertexListTransparent = new ArrayList<>();
		
		
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
				for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
					final BlockType block = chunk.getBlockAt(x, y, z);
					
					if (block == null || Veya.ignoreBlocks.contains(block)) {
						continue;
					}
					
					final boolean solid = block.isSolid();
					
					final int worldX = chunk.getChunkX() * Chunk.CHUNK_SIZE + x;
					final int worldY = chunk.getChunkY() * Chunk.CHUNK_SIZE + y;
					final int worldZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE + z;

					final List<Vertex> vertexList = solid ? vertexListSolid : vertexListTransparent;
					final List<Integer> indexList = solid ? indexListSolid : indexListTransparent;
					
					for (final Side side : Side.values()) {
						final BlockType neighbor = chunk.getNeighborBlock(x, y, z, side);

						if (checkBlockFace(solid, neighbor)) {
							ChunkVAO.addSideOfBlock(indexList, vertexList, block, worldX, worldY, worldZ, side);
						}
					}
				}
			}
		}
		
		holdsSolid = !vertexListSolid.isEmpty();
		holdsTransparent = !vertexListTransparent.isEmpty();
		
		if (holdsSolid) {
			index_solid = Ints.toArray(indexListSolid);
			vertex_data_solid = vertexListSolid.toArray(new Vertex[vertexListSolid.size()]);
			
			this.index_length_solid = index_solid.length;
			this.ibo_buffer_solid = BufferUtils.createIntBuffer(index_solid.length);
			this.ibo_buffer_solid.put(index_solid);
			this.ibo_buffer_solid.flip();
			
			this.vbo_buffer_solid = BufferUtils.createFloatBuffer(vertex_data_solid.length * Vertex.elementCount);
			for (final Vertex vertex : vertex_data_solid) {
				this.vbo_buffer_solid.put(vertex.getElements());
			}
			this.vbo_buffer_solid.flip();
		}
		
		if (holdsTransparent) {
			index_transparent = Ints.toArray(indexListTransparent);
			vertex_data_transparent = vertexListTransparent.toArray(new Vertex[vertexListTransparent.size()]);
			
			this.index_length_transparent = index_transparent.length;
			
			this.ibo_buffer_transparent = BufferUtils.createIntBuffer(index_transparent.length);
			this.ibo_buffer_transparent.put(index_transparent);
			this.ibo_buffer_transparent.flip();
			
			this.vbo_buffer_transparent = BufferUtils.createFloatBuffer(vertex_data_transparent.length * Vertex.elementCount);
			for (final Vertex vertex : vertex_data_transparent) {
				this.vbo_buffer_transparent.put(vertex.getElements());
			}
			this.vbo_buffer_transparent.flip();
		}
		
	}
	
	private boolean checkBlockFace(final boolean solid, final BlockType neighborBlock) {
		return neighborBlock == null || Veya.ignoreBlocks.contains(neighborBlock) || (solid && !neighborBlock.isSolid());
	}
	
	public void create() {
		// create objects
		
		if (holdsSolid) {
			this.ibo_handle_solid = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_solid);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_buffer_solid, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			this.vbo_handle_solid = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_solid);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbo_buffer_solid, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			this.vao_handle_solid = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(this.vao_handle_solid);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_solid);
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexPosition"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexColor"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexTexturePosition"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexNormal"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);
			
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_solid);
			
			GL30.glBindVertexArray(0);
		}
		
		if (holdsTransparent) {
			this.ibo_handle_transparent = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_transparent);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_buffer_transparent, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			this.vbo_handle_transparent = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_transparent);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbo_buffer_transparent, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			
			this.vao_handle_transparent = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(this.vao_handle_transparent);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_transparent);
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexPosition"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexColor"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexTexturePosition"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);
			
			GL20.glEnableVertexAttribArray(Veya.program.getAttribLocation("vertexNormal"));
			GL20.glVertexAttribPointer(Veya.program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);
			
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_transparent);
			
			GL30.glBindVertexArray(0);
		}
		
		
		// System.out.println("created ChunkVAO with " + this.vertexData.length + " vertices");
	}
	
	public void render(final boolean solid) {
		
		try {
			if (solid) {
				if (holdsSolid) {
					GL30.glBindVertexArray(this.vao_handle_solid);
					GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_solid, GL11.GL_UNSIGNED_INT, 0);
					GL30.glBindVertexArray(0);
				}
			} else {
				if (holdsTransparent) {
					GL30.glBindVertexArray(this.vao_handle_transparent);
					GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_transparent, GL11.GL_UNSIGNED_INT, 0);
					GL30.glBindVertexArray(0);
				}
			}
		} catch (final OpenGLException e) {
			System.out.println("vao_handle_solid: " + this.vao_handle_solid);
			e.printStackTrace();
		}
		
	}
	
	public void dispose() {
		
		if (holdsSolid) {
			
			GL30.glDeleteVertexArrays(this.vao_handle_solid);
			GL15.glDeleteBuffers(this.ibo_handle_solid);
			GL15.glDeleteBuffers(this.vbo_handle_solid);
			
			this.vao_handle_solid = -1;
			this.vbo_handle_solid = -1;
			this.ibo_handle_solid = -1;
		}
		
		if (holdsTransparent) {
			GL30.glDeleteVertexArrays(this.vao_handle_transparent);
			GL15.glDeleteBuffers(this.ibo_handle_transparent);
			GL15.glDeleteBuffers(this.vbo_handle_transparent);
			
			this.vao_handle_transparent = -1;
			this.vbo_handle_transparent = -1;
			this.ibo_handle_transparent = -1;
		}
		
	}
	
	private static void addIndexEntries(final List<Integer> indexDataList, final int firstIndex) {
		indexDataList.add(firstIndex);
		indexDataList.add(firstIndex + 1);
		indexDataList.add(firstIndex + 2);
		indexDataList.add(firstIndex + 3);
		indexDataList.add(Veya.RESTART);
	}

	private static void addSideOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ, final Side side) {
		final int firstIndex = vertexDataList.size();
		switch (side) {
			case TOP:
				addTopOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
			case BOTTOM:
				addBottomOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
			case NORTH:
				addNorthOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
			case SOUTH:
				addSouthOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
			case EAST:
				addEastOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
			case WEST:
				addWestOfBlock(vertexDataList, block, worldX, worldY, worldZ);
				break;
		}
		addIndexEntries(indexDataList, firstIndex);
	}
	
	private static void addBottomOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0));
	}

	private static void addTopOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0));
	}
	
	private static void addSouthOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1));
	}
	
	private static void addNorthOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1));
	}
	
	private static void addWestOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0));
		vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0));
	}
	
	private static void addEastOfBlock(final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0));
		vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0));
	}
}
