/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
import eu.over9000.veya.world.World;

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
							ChunkVAO.addSideOfBlock(chunk, indexList, vertexList, block, worldX, worldY, worldZ, side);
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
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);

			GL20.glEnableVertexAttribArray(1);
			GL20.glVertexAttribPointer(1, Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);

			GL20.glEnableVertexAttribArray(2);
			GL20.glVertexAttribPointer(2, Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);

			GL20.glEnableVertexAttribArray(3);
			GL20.glVertexAttribPointer(3, Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);

			GL20.glEnableVertexAttribArray(4);
			GL20.glVertexAttribPointer(4, Vertex.aoElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.aoByteOffset);

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
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);

			GL20.glEnableVertexAttribArray(1);
			GL20.glVertexAttribPointer(1, Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteOffset);

			GL20.glEnableVertexAttribArray(2);
			GL20.glVertexAttribPointer(2, Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);

			GL20.glEnableVertexAttribArray(3);
			GL20.glVertexAttribPointer(3, Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);

			GL20.glEnableVertexAttribArray(4);
			GL20.glVertexAttribPointer(4, Vertex.aoElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.aoByteOffset);

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
					if (Veya.wireframeSwitch) {
						GL11.glDrawElements(GL11.GL_LINE_STRIP, this.index_length_solid, GL11.GL_UNSIGNED_INT, 0);
					} else {
						GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_solid, GL11.GL_UNSIGNED_INT, 0);
					}
					GL30.glBindVertexArray(0);
				}
			} else {
				if (holdsTransparent) {
					GL30.glBindVertexArray(this.vao_handle_transparent);
					if (Veya.wireframeSwitch) {
						GL11.glDrawElements(GL11.GL_LINE_STRIP, this.index_length_transparent, GL11.GL_UNSIGNED_INT, 0);
					} else {
						GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_transparent, GL11.GL_UNSIGNED_INT, 0);
					}
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

	private static void addSideOfBlock(final Chunk chunk, final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ, final Side side) {
		final int firstIndex = vertexDataList.size();
		switch (side) {
			case TOP:
				addTopOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
			case BOTTOM:
				addBottomOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
			case NORTH:
				addNorthOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
			case SOUTH:
				addSouthOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
			case EAST:
				addEastOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
			case WEST:
				addWestOfBlock(chunk, vertexDataList, block, worldX, worldY, worldZ);
				break;
		}
		addIndexEntries(indexDataList, firstIndex);
	}

	private static void addBottomOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.BOTTOM, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.BOTTOM, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.BOTTOM, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.BOTTOM, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao01));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao00));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao11));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao10));
		} else {
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao11));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao10));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.BOTTOM), 0, -1, 0, ao00));
		}
	}

	private static void addTopOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.TOP, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.TOP, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.TOP, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.TOP, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao11));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao00));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao10));
		} else {
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao00));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao10));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.TOP), 0, 1, 0, ao11));
		}
	}

	private static void addSouthOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.SOUTH, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.SOUTH, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.SOUTH, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.SOUTH, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao01));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao00));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao11));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao10));

		} else {
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao11));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao10));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureID(Side.SOUTH), 0, 0, 1, ao00));
		}
	}

	private static void addNorthOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.NORTH, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.NORTH, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.NORTH, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.NORTH, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao11));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao00));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao10));

		} else {
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao00));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao10));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.NORTH), 0, 0, -1, ao11));
		}
	}

	private static void addWestOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.WEST, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.WEST, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.WEST, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.WEST, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao01));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao11));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao00));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao10));
		} else {
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao00));
			vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao01));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao10));
			vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.WEST), -1, 0, 0, ao11));
		}
	}

	private static void addEastOfBlock(final Chunk chunk, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
		final float ao00 = calcAOOfVertex(chunk, Side.EAST, worldX, worldY, worldZ, -1, -1);
		final float ao01 = calcAOOfVertex(chunk, Side.EAST, worldX, worldY, worldZ, -1, +1);
		final float ao10 = calcAOOfVertex(chunk, Side.EAST, worldX, worldY, worldZ, +1, -1);
		final float ao11 = calcAOOfVertex(chunk, Side.EAST, worldX, worldY, worldZ, +1, +1);

		if (ao00 + ao11 > ao01 + ao10) {
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao00));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao11));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao10));
		} else {
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao11));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao01));
			vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao10));
			vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureID(Side.EAST), 1, 0, 0, ao00));
		}
	}

	private static float calcAOOfVertex(final Chunk chunk, final Side side, final int worldX, final int worldY, final int worldZ, final int offsetSide0, final int offsetSide1) {

		final World world = chunk.getWorld();

		final int block10;
		final int block01;
		final int block11;

		switch (side) {
			case TOP:
				block10 = checkIsSolid(world, worldX + offsetSide0, worldY + 1, worldZ);
				block01 = checkIsSolid(world, worldX, worldY + 1, worldZ + offsetSide1);
				block11 = checkIsSolid(world, worldX + offsetSide0, worldY + 1, worldZ + offsetSide1);
				break;

			case BOTTOM:
				block10 = checkIsSolid(world, worldX + offsetSide0, worldY - 1, worldZ);
				block01 = checkIsSolid(world, worldX, worldY - 1, worldZ + offsetSide1);
				block11 = checkIsSolid(world, worldX + offsetSide0, worldY - 1, worldZ + offsetSide1);
				break;

			case NORTH:
				block10 = checkIsSolid(world, worldX + offsetSide0, worldY, worldZ - 1);
				block01 = checkIsSolid(world, worldX, worldY + offsetSide1, worldZ - 1);
				block11 = checkIsSolid(world, worldX + offsetSide0, worldY + offsetSide1, worldZ - 1);
				break;

			case SOUTH:
				block10 = checkIsSolid(world, worldX + offsetSide0, worldY, worldZ + 1);
				block01 = checkIsSolid(world, worldX, worldY + offsetSide1, worldZ + 1);
				block11 = checkIsSolid(world, worldX + offsetSide0, worldY + offsetSide1, worldZ + 1);
				break;

			case EAST:
				block10 = checkIsSolid(world, worldX + 1, worldY + offsetSide0, worldZ);
				block01 = checkIsSolid(world, worldX + 1, worldY, worldZ + offsetSide1);
				block11 = checkIsSolid(world, worldX + 1, worldY + offsetSide0, worldZ + offsetSide1);
				break;

			case WEST:
				block10 = checkIsSolid(world, worldX - 1, worldY + offsetSide0, worldZ);
				block01 = checkIsSolid(world, worldX - 1, worldY, worldZ + offsetSide1);
				block11 = checkIsSolid(world, worldX - 1, worldY + offsetSide0, worldZ + offsetSide1);
				break;

			default:
				throw new IllegalStateException("unknown side: " + side);
		}


		int res;

		if (block01 == 1 && block10 == 1) {
			res = 0;
		} else {
			res = 3 - (block01 + block10 + block11);
		}

		return res;
	}

	private static int checkIsSolid(final World world, final int worldX, final int worldY, final int worldZ) {
		final BlockType block = world.getBlockAt(worldX, worldY, worldZ);

		int result;
		if (block == null) {
			result = 0;
		} else {
			result = block.isSolid() ? 1 : 0;
		}

		//System.out.printf("type (%d %d %d)=%s result=%d\n", worldX, worldY, worldZ, block, result);

		return result;
	}

}
