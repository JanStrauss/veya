package eu.over9000.veya.util;

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
	
}