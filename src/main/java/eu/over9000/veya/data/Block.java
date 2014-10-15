package eu.over9000.veya.data;

public class Block {
	private final int x;
	private final int y;
	private final int z;
	
	private BlockType type;
	
	private final Chunk chunk;
	
	private long changed;
	
	public Block(final int x, final int y, final int z, final BlockType type, final Chunk chunk) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.chunk = chunk;
		this.changed = 0;
		
	}
	
	public BlockType getType() {
		return this.type;
	}
	
	public void setType(final BlockType type) {
		this.type = type;
		this.changed = System.currentTimeMillis();
		this.chunk.blockChanged();
	}
	
	public void clear() {
		this.chunk.clearBlockAt(this.x, this.y, this.z);
	}
	
	public int getInChunkX() {
		return this.x;
	}
	
	public int getInChunkY() {
		return this.y;
	}
	
	public int getInChunkZ() {
		return this.z;
	}
	
	public int getWorldX() {
		return this.chunk.getChunkX() * Chunk.CHUNK_SIZE + this.x;
	}
	
	public int getWorldY() {
		return this.chunk.getChunkY() * Chunk.CHUNK_SIZE + this.y;
	}
	
	public int getWorldZ() {
		return this.chunk.getChunkZ() * Chunk.CHUNK_SIZE + this.z;
	}
	
	public Chunk getChunk() {
		return this.chunk;
	}
	
	public long getChanged() {
		return this.changed;
	}
	
	public Block getNeighborBottom() {
		try {
			return this.chunk.getBlockAt(this.x, this.y - 1, this.z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborBottom();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(this.x, Chunk.CHUNK_SIZE - 1, this.z);
			}
		}
	}
	
	public Block getNeighborTop() {
		try {
			return this.chunk.getBlockAt(this.x, this.y + 1, this.z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborTop();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(this.x, 0, this.z);
			}
		}
	}
	
	public Block getNeighborNorth() {
		try {
			return this.chunk.getBlockAt(this.x, this.y, this.z - 1);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborNorth();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(this.x, this.y, Chunk.CHUNK_SIZE - 1);
			}
		}
	}
	
	public Block getNeighborSouth() {
		try {
			return this.chunk.getBlockAt(this.x, this.y, this.z + 1);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborSouth();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(this.x, this.y, 0);
			}
		}
	}
	
	public Block getNeighborWest() {
		try {
			return this.chunk.getBlockAt(this.x - 1, this.y, this.z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborWest();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(Chunk.CHUNK_SIZE - 1, this.y, this.z);
			}
		}
	}
	
	public Block getNeighborEast() {
		try {
			return this.chunk.getBlockAt(this.x + 1, this.y, this.z);
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.chunk.getNeighborEast();
			if (neighbor == null) {
				return null;
			} else {
				return neighbor.getBlockAt(0, this.y, this.z);
			}
		}
	}
	
}
