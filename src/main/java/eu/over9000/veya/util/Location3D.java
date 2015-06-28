package eu.over9000.veya.util;

import java.util.ArrayList;
import java.util.List;

import eu.over9000.veya.collision.AABB;

public class Location3D implements Comparable<Location3D> {

	public final int x;
	public final int y;
	public final int z;
	private Location3D centerChunk;

	public Location3D(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location3D(final int x, final int y, final int z, final Location3D center) {
		this.x = x;
		this.y = y;
		this.z = z;

		this.centerChunk = center;
	}

	@Override
	public int compareTo(final Location3D other) {
		return Integer.compare(Location3D.calcVecDistance(this, this.centerChunk), Location3D.calcVecDistance(other, this.centerChunk));
	}

	private static int calcVecDistance(final Location3D pos1, final Location3D pos2) {
		final int dist_x = Location3D.calcDistance(pos1.x, pos2.x);
		final int dist_y = Location3D.calcDistance(pos1.y, pos2.y);
		final int dist_z = Location3D.calcDistance(pos1.z, pos2.z);
		return dist_x * dist_x + dist_y * dist_y + dist_z * dist_z;
	}

	private static int calcDistance(final int coordinate1, final int coordinate2) {
		return Math.abs(coordinate1 - coordinate2);
	}

	public static List<Location3D> getBlocksAround(final int centerX, final int centerY, final int centerZ, final int radius) {
		final List<Location3D> result = new ArrayList<>();
		final Location3D center = new Location3D(centerX, centerY, centerZ);

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
					result.add(new Location3D(x, y, z, center));
				}
			}
		}

		return result;
	}

	public static List<Location3D> getBlocksAround(final AABB newPos, final int radius) {
		final List<Location3D> result = new ArrayList<>();

		final int min_x = (int) (newPos.min[0] - radius);
		final int max_x = (int) (newPos.max[0] + radius);
		final int min_y = (int) (newPos.min[1] - radius);
		final int max_y = (int) (newPos.max[1] + radius);
		final int min_z = (int) (newPos.min[2] - radius);
		final int max_z = (int) (newPos.max[2] + radius);

		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				for (int z = min_z; z <= max_z; z++) {
					result.add(new Location3D(x, y, z));
				}
			}
		}

		return result;
	}
}