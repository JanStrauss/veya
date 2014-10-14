package eu.over9000.veya.data;

import java.util.ArrayList;
import java.util.List;

public class World {
	private final long seed;
	private final String name;
	private final List<Chunk> loadedChunks = new ArrayList<>();
	
	public World(final long seed, final String name) {
		this.seed = seed;
		this.name = name;
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
		final Chunk chunk = new Chunk(this, chunkX, chunkY, chunkZ);
		
		// TODO load real world stuffs
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
				for (int y = 0; y < 11; y++) {
					chunk.setBlockAt(x, y, z, BlockType.STONE);
				}
				chunk.setBlockAt(x, 11, z, BlockType.DIRT);
				chunk.setBlockAt(x, 12, z, BlockType.DIRT);
				chunk.setBlockAt(x, 13, z, BlockType.GRASS);
			}
		}
		
		this.loadedChunks.add(chunk);
		return chunk;
	}
	
	private Chunk getOrLoadChunkAt(final int x, final int y, final int z) {
		for (final Chunk chunk : this.loadedChunks) {
			if (chunk.getChunkX() == x && chunk.getChunkY() == y && chunk.getChunkZ() == z) {
				return chunk;
			}
		}
		return this.loadChunk(x, y, z);
	}
	
	public long getSeed() {
		return this.seed;
	}
	
	public String getName() {
		return this.name;
	}
	
	private static int worldToChunkCoordinate(final int coord) {
		return coord / Chunk.CHUNK_SIZE;
	}
	
	private static int worldToBlockInChunkCoordinate(final int coord) {
		return coord % Chunk.CHUNK_SIZE;
	}
	
}