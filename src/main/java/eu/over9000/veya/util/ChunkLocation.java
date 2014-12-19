package eu.over9000.veya.util;

public class ChunkLocation implements Comparable<ChunkLocation> {
	
	public final int x;
	public final int y;
	public final int z;
	private ChunkLocation centerChunk;
	
	public ChunkLocation(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ChunkLocation(final int x, final int y, final int z, final ChunkLocation center) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.centerChunk = center;
	}
	
	@Override
	public int compareTo(final ChunkLocation other) {
		return Integer.compare(ChunkLocation.calcVecDistance(this, this.centerChunk), ChunkLocation.calcVecDistance(other, this.centerChunk));
	}
	
	private static int calcVecDistance(final ChunkLocation pos1, final ChunkLocation pos2) {
		final int dist_x = ChunkLocation.calcDistance(pos1.x, pos2.x);
		final int dist_y = ChunkLocation.calcDistance(pos1.y, pos2.y);
		final int dist_z = ChunkLocation.calcDistance(pos1.z, pos2.z);
		return dist_x * dist_x + dist_y * dist_y + dist_z * dist_z;
	}
	
	private static int calcDistance(final int coordinate1, final int coordinate2) {
		return Math.abs(coordinate1 - coordinate2);
	}
	
}