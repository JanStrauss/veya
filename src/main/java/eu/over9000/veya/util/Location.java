/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.over9000.veya.util;

import java.util.ArrayList;
import java.util.List;

import eu.over9000.veya.collision.AABB;

public class Location implements Comparable<Location> {

	public final int x;
	public final int y;
	public final int z;
	public Location centerChunk;

	public Location(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location(final int x, final int y, final int z, final Location center) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.centerChunk = center;
	}

	public static List<Location> geLocationsAround(final AABB newPos, final int radius) {
		final List<Location> result = new ArrayList<>();

		final int min_x = (int) (newPos.min[0] - radius);
		final int max_x = (int) (newPos.max[0] + radius);
		final int min_y = (int) (newPos.min[1] - radius);
		final int max_y = (int) (newPos.max[1] + radius);
		final int min_z = (int) (newPos.min[2] - radius);
		final int max_z = (int) (newPos.max[2] + radius);

		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				for (int z = min_z; z <= max_z; z++) {
					result.add(new Location(x, y, z));
				}
			}
		}

		return result;
	}

	private static int calcVecDistance(final Location pos1, final Location pos2) {
		final int dist_x = calcDistance(pos1.x, pos2.x);
		final int dist_y = calcDistance(pos1.y, pos2.y);
		final int dist_z = calcDistance(pos1.z, pos2.z);
		return dist_x * dist_x + dist_y * dist_y + dist_z * dist_z;
	}

	private static int calcDistance(final int coordinate1, final int coordinate2) {
		return Math.abs(coordinate1 - coordinate2);
	}

	public static List<Location> getLocationsAround(final int centerX, final int centerY, final int centerZ, final int radius) {
		final List<Location> result = new ArrayList<>();
		final Location center = new Location(centerX, centerY, centerZ);

		final int min_x = centerX - radius;
		final int max_x = centerX + radius;
		final int min_y = centerY - radius;
		final int max_y = centerY + radius;
		final int min_z = centerZ - radius;
		final int max_z = centerZ + radius;

		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				for (int z = min_z; z <= max_z; z++) {
					// TODO: Sphere
					result.add(new Location(x, y, z, center));
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "Location{" +
				"x=" + x +
				", y=" + y +
				", z=" + z +
				'}';
	}

	@Override
	public int compareTo(final Location other) {
		return Integer.compare(calcVecDistance(this, this.centerChunk), calcVecDistance(other, this.centerChunk));
	}

	public boolean nextTo(final Location other) {
		return MathUtil.nextTo(x, other.x) && MathUtil.nextTo(y, other.y) && MathUtil.nextTo(z, other.z);
	}
}