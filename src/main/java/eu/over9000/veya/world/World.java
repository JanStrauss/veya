package eu.over9000.veya.world;

import java.math.RoundingMode;
import java.util.Objects;
import java.util.Random;

import com.google.common.math.IntMath;

import eu.over9000.veya.util.Location;
import eu.over9000.veya.world.storage.ChunkProvider;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

public class World {
	public static final int MAX_WORLD_HEIGHT = 256;
	public static final int MAX_WORLD_HEIGHT_IN_CHUNKS = World.MAX_WORLD_HEIGHT / Chunk.CHUNK_SIZE;

	private final long seed;
	private final String name;
	private final ChunkProvider provider;
	private final Random random;

	public World(final long seed, final String name) {
		this.seed = seed;
		this.name = name;
		this.random = new Random(seed);
		this.provider = new ChunkProvider(this, false);
	}

	public World(final long seed, final String name, boolean debug) {
		this.seed = seed;
		this.name = name;
		this.random = new Random(seed);

		this.provider = new ChunkProvider(this, debug);
	}

	public BlockType getBlockAt(final Location location, final ChunkRequestLevel level, final boolean create) {
		return getBlockAt(location.x, location.y, location.z, level, create);
	}

	public BlockType getBlockAt(final Location location) {
		return getBlockAt(location.x, location.y, location.z, ChunkRequestLevel.CACHE, false);
	}

	public BlockType getBlockAt(final int x, final int y, final int z) {
		return getBlockAt(x, y, z, ChunkRequestLevel.CACHE, false);
	}

	public BlockType getBlockAt(final int x, final int y, final int z, final ChunkRequestLevel level, final boolean create) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ, level, create);

		if (chunk == null) {
			return null;
		}

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		return chunk.getBlockAt(blockX, blockY, blockZ);
	}

	public void setBlockAt(final int x, final int y, final int z, final BlockType type, final ChunkRequestLevel level, final boolean create) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ, level, create);

		if (chunk == null) {
			return;
		}

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		chunk.setBlockAt(blockX, blockY, blockZ, type);
	}

	public Chunk getChunkAt(final int chunkX, final int chunkY, final int chunkZ, final ChunkRequestLevel level, final boolean create) {
		return this.provider.getChunkAt(chunkX, chunkY, chunkZ, level, create);
	}

	public Chunk getChunkAt(final Location location, final ChunkRequestLevel level, final boolean create) {
		return this.provider.getChunkAt(location.x, location.y, location.z, level, create);
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

	public void setBlockAtIfAir(final int x, final int y, final int z, final BlockType type, final ChunkRequestLevel level, final boolean create) {
		if (this.getBlockAt(x, y, z, level, create) == null) {
			this.setBlockAt(x, y, z, type, level, create);
		}
	}

	public void clearBlockAt(final int x, final int y, final int z, final ChunkRequestLevel level) {
		final int chunkX = World.worldToChunkCoordinate(x);
		final int chunkY = World.worldToChunkCoordinate(y);
		final int chunkZ = World.worldToChunkCoordinate(z);

		final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ, level, false);

		if (chunk == null) {
			return;
		}

		final int blockX = World.worldToBlockInChunkCoordinate(x);
		final int blockY = World.worldToBlockInChunkCoordinate(y);
		final int blockZ = World.worldToBlockInChunkCoordinate(z);

		chunk.clearBlockAt(blockX, blockY, blockZ);
	}

	public int getHighestYAt(final int x, final int z, final ChunkRequestLevel level) {
		for (int chunkY = World.MAX_WORLD_HEIGHT_IN_CHUNKS - 1; chunkY >= 0; chunkY--) {
			final int chunkX = World.worldToChunkCoordinate(x);
			final int chunkZ = World.worldToChunkCoordinate(z);

			final Chunk chunk = this.getChunkAt(chunkX, chunkY, chunkZ, level, false);

			if (chunk == null) {
				continue;
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

	public boolean hasChunkChanged(final Chunk chunk) {
		return provider.hasChunkChanged(chunk);
	}

	public Random getRandom() {
		return random;
	}

	public void onExit() {
		provider.onExit();
	}

	public void clearCache(final Location center, final int cacheRange) {
		provider.clearCache(center, cacheRange);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final World world = (World) o;
		return Objects.equals(name, world.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}