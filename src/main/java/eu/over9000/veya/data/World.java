package eu.over9000.veya.data;

import java.math.RoundingMode;
import java.util.Collection;

import com.google.common.math.IntMath;

import eu.over9000.veya.util.ChunkMap;

public class World {
	private final long seed;
	private final String name;
	private final ChunkMap chunks = new ChunkMap();
	
	public World(final long seed, final String name) {
		this.seed = seed;
		this.name = name;
	}
	
	public Collection<Chunk> getLoadedChunks() {
		return this.chunks.getChunks();
	}
	
	public Block getBlockAt(final int x, final int y, final int z) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);
		
		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ);
		
		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);
		
		return chunk.getBlockAt(blockX, blockY, blockZ);
		
	}
	
	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);
		
		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ);
		
		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);
		
		chunk.setBlockAt(blockX, blockY, blockZ, type);
	}
	
	public Block getHighestBlockAt(final int x, final int z) {
		return null; // TODO
	}
	
	public Chunk getChunkAt(final int chunkX, final int chunkY, final int chunkZ) {
		return this.getOrLoadChunkAt(chunkX, chunkY, chunkZ);
	}
	
	private Chunk loadChunk(final int chunkX, final int chunkY, final int chunkZ) {
		// System.out.println("loaded chunk: " + chunkX + ", " + chunkY + ", " + chunkZ);
		final Chunk chunk = new Chunk(this, chunkX, chunkY, chunkZ);
		
		// TODO load real world stuffs
		
		this.chunks.setChunkAt(chunkX, chunkY, chunkZ, chunk);
		return chunk;
	}
	
	private Chunk getOrLoadChunkAt(final int chunkX, final int chunkY, final int chunkZ) {
		final Chunk chunk = this.chunks.getChunkAt(chunkX, chunkY, chunkZ);
		if (chunk == null) {
			return this.loadChunk(chunkX, chunkY, chunkZ);
		} else {
			return chunk;
		}
		
	}
	
	public long getSeed() {
		return this.seed;
	}
	
	public String getName() {
		return this.name;
	}
	
	private static int worldToChunkCoordinate(final int coord) {
		// return coord / Chunk.CHUNK_SIZE;
		return IntMath.divide(coord, Chunk.CHUNK_SIZE, RoundingMode.FLOOR);
	}
	
	private static int worldToBlockInChunkCoordinate(final int coord) {
		int val = coord % Chunk.CHUNK_SIZE;
		if (val < 0) {
			val = val + Chunk.CHUNK_SIZE;
		}
		return val;
	}
	
	private static int chunkToWorldCoordinate(final int coord, final int chunk) {
		return chunk * Chunk.CHUNK_SIZE + coord;
	}
	
	public static void main(final String[] args) {
		
	}
	
}