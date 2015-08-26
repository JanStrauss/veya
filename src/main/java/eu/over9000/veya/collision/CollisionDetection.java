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

package eu.over9000.veya.collision;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.util.Location;

/**
 * Created by Jan on 22.06.2015.
 */
public class CollisionDetection {

	private static final int NUMDIM = 3;
	private static final int RIGHT = 1;
	private static final int LEFT = -1;
	private static final int MIDDLE = 0;

	public static boolean checkCollision(final AABB camera, final List<Location> locations) {
		for (final Location location : locations) {
			final AABB block = new AABB(location);
			if (checkCollision(camera, block)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkCollision(final AABB camera, final AABB block) {
		for (int i = 0; i < NUMDIM; i++) {
			if (!(camera.min[i] <= block.max[i] && camera.max[i] >= block.min[i])) {
				return false;
			}
		}
		return true;

	}

	public static int[] checkCollision(final Vector3f start, final Vector3f direction, final int worldX, final int worldY, final int worldZ) {
		final float[] origin = new float[]{start.x, start.y, start.z};
		final float[] dir = new float[]{direction.x, direction.y, direction.z};
		final float[] minB = new float[]{worldX, worldY, worldZ};
		final float[] maxB = new float[]{worldX + 1, worldY + 1, worldZ + 1};

		return checkHitBoundingBox(minB, maxB, origin, dir);
	}

	private static int[] checkHitBoundingBox(final float[] minB, final float[] maxB, final float[] origin, final float[] dir) {
		boolean inside = true;
		final int[] quadrant = new int[NUMDIM];
		int whichPlane;
		final float[] maxT = new float[NUMDIM];
		final float[] candidatePlane = new float[NUMDIM];
		float[] coord = new float[NUMDIM];

		/* Find candidate planes; this loop can be avoided if
   		rays cast all from the eye(assume perspective view) */
		for (int i = 0; i < NUMDIM; i++) {
			if (origin[i] < minB[i]) {
				quadrant[i] = LEFT;
				candidatePlane[i] = minB[i];
				inside = false;
			} else if (origin[i] > maxB[i]) {
				quadrant[i] = RIGHT;
				candidatePlane[i] = maxB[i];
				inside = false;
			} else {
				quadrant[i] = MIDDLE;
			}
		}

		/* Ray origin inside bounding box */
		if (inside) {
			coord = origin;
			return null;
		}


		/* Calculate T distances to candidate planes */
		for (int i = 0; i < NUMDIM; i++) {
			if (quadrant[i] != MIDDLE && dir[i] != 0f) {
				maxT[i] = (candidatePlane[i] - origin[i]) / dir[i];
			} else {
				maxT[i] = -1f;
			}
		}

		/* Get largest of the maxT's for final choice of intersection */
		whichPlane = 0;
		for (int i = 1; i < NUMDIM; i++) {
			if (maxT[whichPlane] < maxT[i]) {
				whichPlane = i;
			}
		}

		/* Check final candidate actually inside box */
		if (maxT[whichPlane] < 0f) {
			return null;
		}
		for (int i = 0; i < NUMDIM; i++) {
			if (whichPlane != i) {
				coord[i] = origin[i] + maxT[whichPlane] * dir[i];
				if (coord[i] < minB[i] || coord[i] > maxB[i]) {
					return null;
				}
			} else {
				coord[i] = candidatePlane[i];
			}
		}

		return new int[]{whichPlane, quadrant[whichPlane]};		/* ray hits box */
	}

	public static Location getNeighborBlockFromIntersectionResult(final int x, final int y, final int z, final int[] intersectionResult) {
		final int[] arrayLocation = new int[]{x, y, z};
		arrayLocation[intersectionResult[0]] = arrayLocation[intersectionResult[0]] + intersectionResult[1];
		return new Location(arrayLocation[0], arrayLocation[1], arrayLocation[2]);
	}
}
