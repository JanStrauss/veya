package eu.over9000.veya;

public enum BlockType {
	
	STONE(3), DIRT(0), GRASS(0, 1, 2), TEST(5, 4, 6, 7, 8, 9);
	
	private final int textureIDBottom;
	private final int textureIDTop;
	private final int textureIDNorth;
	private final int textureIDEast;
	private final int textureIDSouth;
	private final int textureIDWest;
	
	private BlockType(final int textureIDAll) {
		this.textureIDBottom = textureIDAll;
		this.textureIDTop = textureIDAll;
		this.textureIDNorth = textureIDAll;
		this.textureIDEast = textureIDAll;
		this.textureIDSouth = textureIDAll;
		this.textureIDWest = textureIDAll;
	}
	
	private BlockType(final int textureIDBottom, final int textureIDTop, final int textureIDAll) {
		this.textureIDBottom = textureIDBottom;
		this.textureIDTop = textureIDTop;
		this.textureIDNorth = textureIDAll;
		this.textureIDEast = textureIDAll;
		this.textureIDSouth = textureIDAll;
		this.textureIDWest = textureIDAll;
	}
	
	private BlockType(final int textureIDBottom, final int textureIDTop, final int textureIDNorth, final int textureIDEast, final int textureIDSouth, final int textureIDWest) {
		this.textureIDBottom = textureIDBottom;
		this.textureIDTop = textureIDTop;
		this.textureIDNorth = textureIDNorth;
		this.textureIDEast = textureIDEast;
		this.textureIDSouth = textureIDSouth;
		this.textureIDWest = textureIDWest;
	}
	
	public int[] getTextureLookupArray() {
		return new int[] { this.textureIDBottom, this.textureIDTop, this.textureIDNorth, this.textureIDEast, this.textureIDSouth, this.textureIDWest };
	}
	
}
