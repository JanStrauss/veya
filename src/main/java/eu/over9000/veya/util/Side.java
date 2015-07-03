package eu.over9000.veya.util;

/**
 * Created by Jan on 03.07.2015.
 */
public enum Side {
	TOP(0,1,0),
	BOTTOM(0,-1,0),
	NORTH(0,0,-1),
	SOUTH(0,0,1),
	EAST(1,0,0),
	WEST(-1,0,0);

	private final int offsetX;
	private final int offsetY;
	private final int offsetZ;

	Side(final int offsetX, final int offsetY, final int offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public int getOffsetZ() {
		return offsetZ;
	}
}
