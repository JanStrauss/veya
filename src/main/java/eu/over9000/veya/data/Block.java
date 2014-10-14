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
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
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
			return null;
		}
	}
	
	public Block getNeighborTop() {
		try {
			return this.chunk.getBlockAt(this.x, this.y + 1, this.z);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	public Block getNeighborNorth() {
		try {
			return this.chunk.getBlockAt(this.x, this.y, this.z - 1);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	public Block getNeighborSouth() {
		try {
			return this.chunk.getBlockAt(this.x, this.y, this.z + 1);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	public Block getNeighborWest() {
		try {
			return this.chunk.getBlockAt(this.x - 1, this.y, this.z);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	public Block getNeighborEast() {
		try {
			return this.chunk.getBlockAt(this.x + 1, this.y, this.z);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
}
