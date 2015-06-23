package eu.over9000.veya.model;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.math.IntMath;

import eu.over9000.veya.util.ChunkMap;
import eu.over9000.veya.util.Location3D;
import eu.over9000.veya.generation.WorldGenerator;
import eu.over9000.veya.generation.WorldPopulator;

public class World {
	public static final int MAX_WORLD_HEIGHT = 256;
	public static final int MAX_WORLD_HEIGHT_IN_CHUNKS = World.MAX_WORLD_HEIGHT / Chunk.CHUNK_SIZE;

	private final long seed;
	private final String name;
	private final ChunkMap chunks = new ChunkMap();
	private final Random random;

	public World(final long seed, final String name) {
		this.seed = seed;
		this.name = name;
		this.random = new Random(seed);
	}

	public Collection<Chunk> getLoadedChunks() {
		return this.chunks.getChunks();
	}

	public BlockType getBlockAt(final int x, final int y, final int z) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAtInternal(chunkX, chunkY, chunkZ);

		if (chunk == null) {
			return null;
		}

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		return chunk.getBlockAt(blockX, blockY, blockZ);
	}

	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAtInternal(chunkX, chunkY, chunkZ);

		if (chunk == null) {
			return;
		}

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		chunk.setBlockAt(blockX, blockY, blockZ, type);
	}

	public Chunk getChunkAtInternal(final int chunkX, final int chunkY, final int chunkZ) {
		return this.chunks.getChunkAt(chunkX, chunkY, chunkZ);
	}

	public Chunk getChunkAt(final int chunkX, final int chunkY, final int chunkZ) {
		generateChunkStackAt(chunkX, chunkZ);
		populateChunkStackAt(chunkX, chunkZ);
		return this.chunks.getChunkAt(chunkX, chunkY, chunkZ);
	}

	private void populateChunkStackAt(final int chunkX, final int chunkZ) {
		final ChunkMap.ChunkState state = chunks.getChunkState(chunkX, chunkZ);

		if (state.populated) {
			return;
		}

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				generateChunkStackAt(chunkX + x, chunkZ + z);
			}
		}

		WorldPopulator.populateChunkStack(this, random, chunkX, chunkZ);

		state.populated = true;
	}

	private void generateChunkStackAt(final int chunkX, final int chunkZ) {
		final ChunkMap.ChunkState state = chunks.getChunkState(chunkX, chunkZ);

		if (state.generated) {
			return;
		}

		final List<Chunk> newChunks = WorldGenerator.genChunksAt(this, random, chunkX, chunkZ);
		for (final Chunk chunk : newChunks) {
			chunks.setChunkAt(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ(), chunk);
		}

		state.generated = true;
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

	public void setBlockAtIfAir(final int x, final int y, final int z, final BlockType type) {
		if (this.getBlockAt(x, y, z) == null) {
			this.setBlockAt(x, y, z, type);
		}
	}

	public void clearBlockAt(final int x, final int y, final int z) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAtInternal(chunkX, chunkY, chunkZ);

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		chunk.clearBlockAt(blockX, blockY, blockZ);
	}

	public List<Location3D> getBlocksAround(final int centerX, final int centerY, final int centerZ, final int radius) {
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

	public int getHighestYAt(final int x, final int z) {
		for (int chunkY = World.MAX_WORLD_HEIGHT_IN_CHUNKS - 1; chunkY >= 0; chunkY--) {
			final int chunkX = World.worldToChunkCoordinate(x);
			final int chunkZ = World.worldToChunkCoordinate(z);

			final Chunk chunk = this.getChunkAtInternal(chunkX, chunkY, chunkZ);

			if (chunk == null) {
				throw new IllegalStateException("getHighestYAt called for ungenerated chunk");
			}

			for (int blockY = Chunk.CHUNK_SIZE - 1; blockY >= 0; blockY--) {
				final int blockX = World.worldToBlockInChunkCoordinate(x);
				final int blockZ = World.worldToBlockInChunkCoordinate(z);

				if (chunk.getBlockAt(blockX, blockY, blockZ) != null) {
					return World.chunkToWorldCoordinate(blockY, chunkY);
				}
			}
		}
		return 0;
	}

}