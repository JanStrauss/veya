package eu.over9000.veya.collision;

import eu.over9000.veya.util.Location3D;

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

	public AABB(final Location3D location) {
		min = new float[]{location.x, location.y, location.z};
		max = new float[]{location.x + 1, location.y + 1, location.z + 1};
	}
}
