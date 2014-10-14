package eu.over9000.veya.data;

import java.util.Objects;

public class Chunk {
	public static final int CHUNK_SIZE = 32;
	
	private final World world;
	
	private final int chunkX;
	private final int chunkY;
	private final int chunkZ;
	
	private final Block[][][] blocks = new Block[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
	
	private boolean changed = true;
	
	public Chunk(final World world, final int chunkX, final int chunkY, final int chunkZ) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
	}
	
	public Block getBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);
		
		return this.blocks[x][y][z];
	}
	
	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		Chunk.checkParameters(x, y, z);
		
		Block block = this.blocks[x][y][z];
		if (block == null) {
			block = new Block(x, y, z, type, this);
			this.blocks[x][y][z] = block;
		} else {
			block.setType(type);
		}
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
}
