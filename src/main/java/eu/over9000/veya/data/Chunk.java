package eu.over9000.veya.data;

import java.util.Objects;

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
	
	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		Chunk.checkParameters(x, y, z);
		
		this.blocks[x][y][z] = type;
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
			throw new IllegalArgumentException("x value (" + x + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}
		
		if (0 > y || y >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("y value (" + y + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}
		
		if (0 > z || z >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("z value (" + z + ")  is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}
	}
	
	public void blockChanged() {
		this.changed = true;
	}
	
	public Chunk getNeighborChunkBottom() {
		return this.world.getChunkNoGenAt(this.chunkX, this.chunkY - 1, this.chunkZ);
	}
	
	public Chunk getNeighborChunkTop() {
		return this.world.getChunkNoGenAt(this.chunkX, this.chunkY + 1, this.chunkZ);
	}
	
	public Chunk getNeighborChunkNorth() {
		return this.world.getChunkNoGenAt(this.chunkX, this.chunkY, this.chunkZ - 1);
	}
	
	public Chunk getNeighborChunkSouth() {
		return this.world.getChunkNoGenAt(this.chunkX, this.chunkY, this.chunkZ + 1);
	}
	
	public Chunk getNeighborChunkWest() {
		return this.world.getChunkNoGenAt(this.chunkX - 1, this.chunkY, this.chunkZ);
	}
	
	public Chunk getNeighborChunkEast() {
		return this.world.getChunkNoGenAt(this.chunkX + 1, this.chunkY, this.chunkZ);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.world, this.chunkX, this.chunkY, this.chunkZ);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Chunk)) {
			return false;
		}
		final Chunk other = (Chunk) obj;
		if (!this.world.equals(other.world)) {
			return false;
		}
		
		if (this.chunkX != other.chunkX) {
			return false;
		}
		
		if (this.chunkY != other.chunkY) {
			return false;
		}
		
		if (this.chunkZ != other.chunkZ) {
			return false;
		}
		
		return true;
	}
	
	public BlockType getNeighborBlockBottom(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x, y - 1, z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkBottom();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(x, Chunk.CHUNK_SIZE - 1, z);
			}
		}
	}
	
	public BlockType getNeighborBlockTop(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x, y + 1, z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkTop();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(x, 0, z);
			}
		}
	}
	
	public BlockType getNeighborBlockNorth(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x, y, z - 1);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkNorth();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(x, y, Chunk.CHUNK_SIZE - 1);
			}
		}
	}
	
	public BlockType getNeighborBlockSouth(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x, y, z + 1);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkSouth();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(x, y, 0);
			}
		}
	}
	
	public BlockType getNeighborBlockWest(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x - 1, y, z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkWest();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(Chunk.CHUNK_SIZE - 1, y, z);
			}
		}
	}
	
	public BlockType getNeighborBlockEast(final int x, final int y, final int z) {
		try {
			return this.getBlockAt(x + 1, y, z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.getNeighborChunkEast();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(0, y, z);
			}
		}
	}
	
	public void clearBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);
		
		this.blocks[x][y][z] = null;
		this.blockChanged();
	}
	
	public void assertNeighborsGenerated() {
		this.world.getChunkWithGenAt(this.chunkX, this.chunkY, this.chunkZ - 1);
		this.world.getChunkWithGenAt(this.chunkX, this.chunkY, this.chunkZ + 1);
		this.world.getChunkWithGenAt(this.chunkX - 1, this.chunkY, this.chunkZ);
		this.world.getChunkWithGenAt(this.chunkX + 1, this.chunkY, this.chunkZ);
	}
}
