package eu.over9000.veya.data;

import java.math.RoundingMode;
import java.util.Collection;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.google.common.math.IntMath;

import eu.over9000.veya.util.ChunkMap;
import eu.over9000.veya.util.WorldGen;

public class World {
	public static final int MAX_WORLD_HEIGHT = 256;
	public static final int MAX_WORLD_HEIGHT_IN_CHUNKS = World.MAX_WORLD_HEIGHT / Chunk.CHUNK_SIZE;
	
	private final long seed;
	private final String name;
	private final ChunkMap chunks = new ChunkMap();
	private final WorldGen generator;
	
	public World(final long seed, final String name) {
		this.seed = seed;
		this.name = name;
		this.generator = new WorldGen(this);
	}
	
	public Collection<Chunk> getLoadedChunks() {
		return this.chunks.getChunks();
	}
	
	public BlockType getBlockAt(final int x, final int y, final int z) {
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
	
	public BlockType getHighestBlockAt(final int x, final int z) {
		throw new NotImplementedException(); // TODO
	}
	
	public Chunk getChunkAt(final int chunkX, final int chunkY, final int chunkZ) {
		return this.getOrLoadChunkAt(chunkX, chunkY, chunkZ);
	}
	
	public Chunk getChunkNoGenAt(final int chunkX, final int chunkY, final int chunkZ) {
		return this.chunks.getChunkAt(chunkX, chunkY, chunkZ);
	}
	
	public Chunk getChunkWithGenAt(final int chunkX, final int chunkY, final int chunkZ) {
		if (this.chunks.isGenerated(chunkX, chunkZ)) {
			return this.getChunkNoGenAt(chunkX, chunkY, chunkZ);
		} else {
			this.generator.genChunksAt(chunkX, chunkZ);
			this.chunks.markAsGenerated(chunkX, chunkZ);
			return this.getChunkNoGenAt(chunkX, chunkY, chunkZ);
		}
	}
	
	private Chunk loadChunk(final int chunkX, final int chunkY, final int chunkZ) {
		final Chunk chunk = new Chunk(this, chunkX, chunkY, chunkZ);
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
	
	public static int worldToChunkCoordinate(final int coord) {
		return IntMath.divide(coord, Chunk.CHUNK_SIZE, RoundingMode.FLOOR);
	}
	
	public static int worldToBlockInChunkCoordinate(final int coord) {
		int val = coord % Chunk.CHUNK_SIZE;
		if (val < 0) {
			val = val + Chunk.CHUNK_SIZE;
		}
		return val;
	}
	
	public static int chunkToWorldCoordinate(final int coord, final int chunk) {
		return chunk * Chunk.CHUNK_SIZE + coord;
	}
	
	public static void main(final String[] args) {
		
	}
	
	public void setBlockAtIfAir(final int x, final int y, final int z, final BlockType type) {
		if (this.getBlockAt(x, y, z) == null) {
			this.setBlockAt(x, y, z, type);
		}
	}
	
	public void clearBlockAt(final int x, final int y, final int z) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);
		
		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ);
		
		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);
		
		chunk.clearBlockAt(blockX, blockY, blockZ);
	}
	
	public void genStartChunks(final int size) {
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
				
				this.generator.genChunksAt(x, z);
				this.chunks.markAsGenerated(x, z);
				
			}
		}
	}
	
}