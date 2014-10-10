package eu.over9000.veya;

public enum BlockType {
	
	STONE(BlockFace.gen(3, 0)), DIRT(BlockFace.gen(0, 0)), GRASS(BlockFace.gen(1, 0), BlockFace.gen(0, 0), BlockFace.gen(2, 0));
	
	private BlockType(final BlockFace all) {
		this.top = all;
		this.bottom = all;
		this.north = all;
		this.east = all;
		this.south = all;
		this.west = all;
	}
	
	private BlockType(final BlockFace top, final BlockFace bottom, final BlockFace sides) {
		this.top = top;
		this.bottom = bottom;
		this.north = sides;
		this.east = sides;
		this.south = sides;
		this.west = sides;
	}
	
	private BlockType(final BlockFace top, final BlockFace bottom, final BlockFace north, final BlockFace east, final BlockFace south, final BlockFace west) {
		this.top = top;
		this.bottom = bottom;
		this.north = north;
		this.east = east;
		this.south = south;
		this.west = west;
	}
	
	private final BlockFace top;
	private final BlockFace bottom;
	private final BlockFace north;
	private final BlockFace east;
	private final BlockFace south;
	private final BlockFace west;
	
	public BlockFace getTop() {
		return this.top;
	}
	
	public BlockFace getBottom() {
		return this.bottom;
	}
	
	public BlockFace getNorth() {
		return this.north;
	}
	
	public BlockFace getEast() {
		return this.east;
	}
	
	public BlockFace getSouth() {
		return this.south;
	}
	
	public BlockFace getWest() {
		return this.west;
	}
	
}
