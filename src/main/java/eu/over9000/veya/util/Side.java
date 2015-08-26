/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
