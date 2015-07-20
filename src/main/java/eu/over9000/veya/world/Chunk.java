package eu.over9000.veya.world;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

import eu.over9000.veya.util.Location;
import eu.over9000.veya.util.Side;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

public class Chunk {
	public static final int CHUNK_SIZE = 32;
	public static final int DATA_LENGTH = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE;

	private final World world;

	private final Location location;

	private final BlockType[] blocks;

	private final AtomicBoolean changed = new AtomicBoolean(true);

	private final StampedLock readWriteLock = new StampedLock();

	public Chunk(final World world, final int chunkX, final int chunkY, final int chunkZ, final BlockType[] data) {
		this.world = world;
		this.location = new Location(chunkX, chunkY, chunkZ);
		this.blocks = data;
		notifyNeighborChunksOfUpdate();
	}
	
	public Chunk(final World world, final int chunkX, final int chunkY, final int chunkZ) {
		this.world = world;
		this.location = new Location(chunkX, chunkY, chunkZ);
		this.blocks = new BlockType[DATA_LENGTH];
		notifyNeighborChunksOfUpdate();
	}
	
	public static int toIndex(final int x, final int y, final int z) {
		return x + y * Chunk.CHUNK_SIZE + z * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE;
	}

	public BlockType getBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);

		final long lock = readWriteLock.readLock();
		final BlockType type = this.blocks[toIndex(x, y, z)];
		readWriteLock.unlock(lock);

		return type;
	}

	public void setBlockAt(final int x, final int y, final int z, final BlockType type) {
		Chunk.checkParameters(x, y, z);

		final long lock = readWriteLock.writeLock();
		this.blocks[toIndex(x, y, z)] = type;
		readWriteLock.unlock(lock);

		changed();
		notifyNeighborChunksOfUpdate(x, y, z);
	}

	public void clearBlockAt(final int x, final int y, final int z) {
		Chunk.checkParameters(x, y, z);

		final long lock = readWriteLock.writeLock();
		this.blocks[toIndex(x, y, z)] = null;
		readWriteLock.unlock(lock);

		this.changed();
		notifyNeighborChunksOfUpdate(x, y, z);
	}

	public boolean getAndResetChangedFlag() {
		return this.changed.getAndSet(false);
	}

	public boolean isEmpty() {
		final long lock = readWriteLock.readLock();
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
					if (this.blocks[toIndex(x, y, z)] != null) {
						return false;
					}
				}
			}
		}
		readWriteLock.unlock(lock);
		return true;
	}

	public int getChunkX() {
		return this.location.x;
	}

	public int getChunkY() {
		return this.location.y;
	}

	public int getChunkZ() {
		return this.location.z;
	}

	public World getWorld() {
		return this.world;
	}

	private static void checkParameters(final int x, final int y, final int z) {
		if (0 > x || x >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("y value (" + x + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}

		if (0 > y || y >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("y value (" + y + ") is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}

		if (0 > z || z >= Chunk.CHUNK_SIZE) {
			throw new IllegalArgumentException("z value (" + z + ")  is not in valid range of [0," + Chunk.CHUNK_SIZE + "[");
		}
	}

	private void notifyNeighborChunksOfUpdate(final int x, final int y, final int z) {
		final ChunkRequestLevel requestLevel = ChunkRequestLevel.CACHE;
		if (x == 0) {
			final Chunk west = this.world.getChunkAt(location.x - 1, location.y, location.z, requestLevel, false);
			if (west != null) {
				west.changed();
			}
		} else if (x == Chunk.CHUNK_SIZE - 1) {
			final Chunk east = this.world.getChunkAt(location.x + 1, location.y, location.z, requestLevel, false);
			if (east != null) {
				east.changed();
			}
		}

		if (y == 0) {
			final Chunk bottom = this.world.getChunkAt(location.x, location.y - 1, location.z, requestLevel, false);
			if (bottom != null) {
				bottom.changed();
			}
		} else if (y == Chunk.CHUNK_SIZE - 1) {
			final Chunk top = this.world.getChunkAt(location.x, location.y + 1, location.z, requestLevel, false);
			if (top != null) {
				top.changed();
			}
		}

		if (z == 0) {
			final Chunk north = this.world.getChunkAt(location.x, location.y, location.z - 1, requestLevel, false);
			if (north != null) {
				north.changed();
			}
		} else if (z == Chunk.CHUNK_SIZE - 1) {
			final Chunk south = this.world.getChunkAt(location.x, location.y, location.z + 1, requestLevel, false);
			if (south != null) {
				south.changed();
			}
		}
	}

	private void notifyNeighborChunksOfUpdate() {
		notifyNeighborChunksOfUpdate(0, 0, 0);
		notifyNeighborChunksOfUpdate(CHUNK_SIZE - 1, CHUNK_SIZE - 1, CHUNK_SIZE - 1);
	}

	private void changed() {
		this.changed.set(true);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Chunk other = (Chunk) o;
		return Objects.equals(world, other.world) && Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, location);
	}

	public BlockType getNeighborBlock(final int x, final int y, final int z, final Side side) {
		try {
			return this.getBlockAt(x + side.getOffsetX(), y + side.getOffsetY(), z + side.getOffsetZ());
		} catch (final IllegalArgumentException e) {
			final Chunk neighbor = this.world.getChunkAt(this.location.x + side.getOffsetX(), this.location.y + side.getOffsetY(), this.location.z + side.getOffsetZ(), ChunkRequestLevel.CACHE, false);
			if (neighbor == null) {
				return null;
			}

			final int neighborX = Math.floorMod(x + side.getOffsetX(), CHUNK_SIZE);
			final int neighborY = Math.floorMod(y + side.getOffsetY(), CHUNK_SIZE);
			final int neighborZ = Math.floorMod(z + side.getOffsetZ(), CHUNK_SIZE);
			return neighbor.getBlockAt(neighborX, neighborY, neighborZ);
		}
	}

	@Override
	public String toString() {
		return "Chunk{" +
				"world=" + world +
				", location=" + location +
				'}';
	}

	public BlockType[] copyRaw() {
		final BlockType[] copy = new BlockType[DATA_LENGTH];
		final long lock = readWriteLock.readLock();
		System.arraycopy(blocks, 0, copy, 0, DATA_LENGTH);
		readWriteLock.unlock(lock);
		return copy;
	}
	
	public Location getLocation() {
		return location;
	}
}
