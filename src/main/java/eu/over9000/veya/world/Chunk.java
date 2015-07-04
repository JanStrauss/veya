package eu.over9000.veya.world;

import java.util.Objects;

import eu.over9000.veya.util.Side;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

public class Chunk {
	public static final int CHUNK_SIZE = 32;

	private final World world;

	private final int chunkX;
	private final int chunkY;
	private final int chunkZ;

	private final BlockType[][][] blocks = new BlockType[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

	private boolean changed = true;

	public Chunk(final World world, final int chunkX, final int chunkY, final int chunkZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
	}

	public BlockType getBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);
		return this.blocks[x][y][z];
	}

	public void initBlockAt(final int x, final int y, final int z, final BlockType type) {
		Chunk.checkParameters(x, y, z);
		this.blocks[x][y][z] = type;
	}

	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		Chunk.checkParameters(x, y, z);
		this.blocks[x][y][z] = type;
		blockChanged();
		notifyNeighborChunksOfUpdate(x, y, z);
	}

	public void clearBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);
		this.blocks[x][y][z] = null;
		this.blockChanged();
		notifyNeighborChunksOfUpdate(x, y, z);
	}

	public boolean getAndResetChangedFlag() {
		final boolean value = this.changed;
		this.changed = false;
		return value;
	}

	public boolean isEmpty() {
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
					if (this.blocks[x][y][z] != null) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public int getChunkX() {
		return this.chunkX;
	}

	public int getChunkY() {
		return this.chunkY;
	}

	public int getChunkZ() {
		return this.chunkZ;
	}

	public World getWorld() {
		return this.world;
	}

	private static void checkParameters(final int x, final int y, final int z) {
		if (0 > x || x >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("y value (" + x + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}

		if (0 > y || y >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("y value (" + y + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}

		if (0 > z || z >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("z value (" + z + ")  is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}
	}

	private void notifyNeighborChunksOfUpdate(final int x, final int y, final int z) {
		if (x == 0) {
			final Chunk west = this.world.getChunkAt(this.chunkX - 1, this.chunkY, this.chunkZ, ChunkRequestLevel.CACHE, false);
			if (west != null) {
				west.blockChanged();
			}
		} else if (x == Chunk.CHUNK_SIZE - 1) {
			final Chunk east = this.world.getChunkAt(this.chunkX + 1, this.chunkY, this.chunkZ, ChunkRequestLevel.CACHE, false);
			if (east != null) {
				east.blockChanged();
			}
		}

		if (y == 0) {
			final Chunk bottom = this.world.getChunkAt(this.chunkX, this.chunkY - 1, this.chunkZ, ChunkRequestLevel.CACHE, false);
			if (bottom != null) {
				bottom.blockChanged();
			}
		} else if (y == Chunk.CHUNK_SIZE - 1) {
			final Chunk top = this.world.getChunkAt(this.chunkX, this.chunkY + 1, this.chunkZ, ChunkRequestLevel.CACHE, false);
			if (top != null) {
				top.blockChanged();
			}
		}

		if (z == 0) {
			final Chunk north = this.world.getChunkAt(this.chunkX, this.chunkY, this.chunkZ - 1, ChunkRequestLevel.CACHE, false);
			if (north != null) {
				north.blockChanged();
			}
		} else if (z == Chunk.CHUNK_SIZE - 1) {
			final Chunk south = this.world.getChunkAt(this.chunkX, this.chunkY, this.chunkZ + 1, ChunkRequestLevel.CACHE, false);
			if (south != null) {
				south.blockChanged();
			}
		}
	}

	private void blockChanged() {
		this.changed = true;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Chunk chunk = (Chunk) o;
		return Objects.equals(chunkX, chunk.chunkX) &&
				Objects.equals(chunkY, chunk.chunkY) &&
				Objects.equals(chunkZ, chunk.chunkZ) &&
				Objects.equals(world, chunk.world);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, chunkX, chunkY, chunkZ);
	}

	public BlockType getNeighborBlock(final int x, final int y, final int z, final Side side) {
		try {
			return this.getBlockAt(x + side.getOffsetX(), y + side.getOffsetY(), z + side.getOffsetZ());
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.world.getChunkAt(this.chunkX + side.getOffsetX(), this.chunkY + side.getOffsetY(), this.chunkZ + side.getOffsetZ(), ChunkRequestLevel.CACHE, false);
			if (neighbor == null) {
				return null;
			}

			final int neighborX = Math.floorMod(x + side.getOffsetX(), CHUNK_SIZE);
			final int neighborY = Math.floorMod(y + side.getOffsetY(), CHUNK_SIZE);
			final int neighborZ = Math.floorMod(z + side.getOffsetZ(), CHUNK_SIZE);
			return neighbor.getBlockAt(neighborX, neighborY, neighborZ);
		}
	}

	@Override
	public String toString() {
		return "Chunk{" +
				"world=" + world.getName() +
				", X=" + chunkX +
				", Y=" + chunkY +
				", Z=" + chunkZ +
				'}';
	}

}