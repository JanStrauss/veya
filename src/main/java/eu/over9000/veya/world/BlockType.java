package eu.over9000.veya.world;

import eu.over9000.veya.util.Side;

public enum BlockType {

	STONE(3),
	DIRT(0),
	GRASS(0, 1, 2),
	TEST(5, 4, 6, 7, 8, 9),
	LOG_OAK(10, 10, 11),
	LOG_SPRUCE(27, 27, 25),
	LOG_BIRCH(10, 10, 21),
	WATER(12),
	LEAVES_DEFAULT(13),
	LEAVES_SPRUCE(22),
	SAND(14),
	IRON_ORE(15),
	COAL_ORE(23),
	COBBLESTONE(16),
	MOSSY_COBBLESTONE(17),
	GRAVEL(18),
	BEDROCK(19),
	PLANKS_OAK(20),
	PLANKS_SPRUCE(24),
	PLANKS_BIRCH(26);


	private final int textureIDs[] = new int[Side.values().length];

	BlockType(final int textureIDAll) {
		textureIDs[Side.TOP.ordinal()] = textureIDAll;
		textureIDs[Side.BOTTOM.ordinal()] = textureIDAll;
		textureIDs[Side.NORTH.ordinal()] = textureIDAll;
		textureIDs[Side.SOUTH.ordinal()] = textureIDAll;
		textureIDs[Side.EAST.ordinal()] = textureIDAll;
		textureIDs[Side.WEST.ordinal()] = textureIDAll;
	}

	BlockType(final int textureIDBottom, final int textureIDTop, final int textureIDRest) {
		textureIDs[Side.TOP.ordinal()] = textureIDTop;
		textureIDs[Side.BOTTOM.ordinal()] = textureIDBottom;
		textureIDs[Side.NORTH.ordinal()] = textureIDRest;
		textureIDs[Side.SOUTH.ordinal()] = textureIDRest;
		textureIDs[Side.EAST.ordinal()] = textureIDRest;
		textureIDs[Side.WEST.ordinal()] = textureIDRest;
	}

	BlockType(final int textureIDBottom, final int textureIDTop, final int textureIDNorth, final int textureIDEast, final int textureIDSouth, final int textureIDWest) {
		textureIDs[Side.TOP.ordinal()] = textureIDTop;
		textureIDs[Side.BOTTOM.ordinal()] = textureIDBottom;
		textureIDs[Side.NORTH.ordinal()] = textureIDNorth;
		textureIDs[Side.SOUTH.ordinal()] = textureIDSouth;
		textureIDs[Side.EAST.ordinal()] = textureIDEast;
		textureIDs[Side.WEST.ordinal()] = textureIDWest;
	}

	public int getTextureID(final Side side) {
		return textureIDs[side.ordinal()];
	}


	public boolean isSolid() {
		return !this.equals(BlockType.WATER);
	}
}
