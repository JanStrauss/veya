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

package eu.over9000.veya.collision;

import eu.over9000.veya.util.Location;

/**
 * Created by Jan on 25.06.2015.
 */
public class AABB {
	public final float[] min;
	public final float[] max;

	public AABB(final float min_x, final float min_y, final float min_z, final float max_x, final float max_y, final float max_z) {
		min = new float[]{min_x, min_y, min_z};
		max = new float[]{max_x, max_y, max_z};
	}

	public AABB(final Location location) {
		min = new float[]{location.x, location.y, location.z};
		max = new float[]{location.x + 1, location.y + 1, location.z + 1};
	}
}
