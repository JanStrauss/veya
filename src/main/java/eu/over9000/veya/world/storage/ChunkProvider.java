package eu.over9000.veya.world.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.over9000.veya.Veya;
import eu.over9000.veya.util.Location;
import eu.over9000.veya.util.MathUtil;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;
import eu.over9000.veya.world.generation.ChunkGenerator;
import eu.over9000.veya.world.generation.ChunkPopulator;

/**
 * Created by Jan on 28.06.2015.
 */
public class ChunkProvider implements Runnable {

	private final World world;
	private final ChunkCache cache;
	private final ChunkDatabase database;

	private final BlockingQueue<ChunkStack> storeQueue = new LinkedBlockingQueue<>();
	private final Thread storeThread;
	private boolean alive = true;

	public ChunkProvider(final World world) {
		this.world = world;
		this.database = new ChunkDatabase();
		this.cache = new ChunkCache();
		this.storeThread = new Thread(this, "StoreThread");

		database.start();
		storeThread.start();
	}

	public Chunk getChunkAt(final Location location, final ChunkRequestLevel level, final boolean create) {
		return getChunkAt(location.x, location.y, location.z, level, create);
	}

	public Chunk getChunkAt(final int x, final int y, final int z, final ChunkRequestLevel level, final boolean create) {
		Objects.requireNonNull(level);

		ChunkStack stack = cache.getChunkStackAt(x, z);

		if (stack == null && level.includes(ChunkRequestLevel.DATABASE)) {
			stack = getChunkStackFromDB(x, z);
			if (stack == null && level.includes(ChunkRequestLevel.GENERATOR)) {
				stack = getChunkStackFromGenerator(x, z);
			}
		}

		if (level.includes(ChunkRequestLevel.POPULATED)) {
			stack = getChunkStackFromPopulator(x, z);
		}

		if (level.includes(ChunkRequestLevel.NEIGHBORS_LOADED)) {

			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i != 0 && j != 0) {
						getChunkAt(x + i, y, z + j, ChunkRequestLevel.GENERATOR, false);
					}
				}
			}
		}

		Chunk chunk;

		if (stack == null) {
			return null;
		}

		chunk = stack.getChunkAt(y);

		if (chunk == null && create) {
			chunk = new Chunk(world, x, y, z);
			stack.setChunkAt(y, chunk);
			addToQueue(stack);
			Veya.scene.onNewChunk(chunk);
		}

		return chunk;
	}

	private ChunkStack getChunkStackFromDB(final int x, final int z) {
		final ChunkStack stack = database.loadChunkStack(world, x, z);
		if (stack != null) {
			cache.setChunkStackAt(x, z, stack);
		}
		return stack;
	}

	private ChunkStack getChunkStackFromGenerator(final int x, final int z) {
		final ChunkStack stack = ChunkGenerator.genChunksAt(world, world.getRandom(), x, z);

		addToQueue(stack);
		cache.setChunkStackAt(x, z, stack);
		
		return stack;
	}

	private ChunkStack getChunkStackFromPopulator(final int x, final int z) {
		final ChunkStack stack = cache.getChunkStackAt(x, z);

		if (stack != null && !stack.isPopulated()) {
			ChunkPopulator.populateChunkStack(world, world.getRandom(), x, z);
			stack.setPopulated(true);
			addToQueue(stack);
		}
		return stack;
	}

	public boolean hasChunkChanged(final Chunk chunk) {
		if (chunk == null) {
			return false;
		}

		final boolean changed = chunk.getAndResetChangedFlag();
		if (changed) {
			final ChunkStack stack = cache.getChunkStackAt(chunk.getChunkX(), chunk.getChunkZ());
			if (stack != null) {
				addToQueue(stack);
			}
			//System.out.println("Chunk " + chunk + " changed");
		}

		return changed;
	}

	private void addToQueue(final ChunkStack stack) {
		if (!alive) {
			return;
		}

		if (!storeQueue.contains(stack)) {
			storeQueue.add(stack);
		}
	}

	public void onExit() {
		alive = false;
		try {
			storeThread.interrupt();
			storeThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		database.stop();
	}

	@Override
	public void run() {
		while (alive) {
			try {
				database.storeChunkStack(world, storeQueue.take());
			} catch (InterruptedException e) {
				if (!alive) {
					break;
				}
				e.printStackTrace();
			}
		}
		ChunkStack stack;
		while ((stack = storeQueue.poll()) != null) {
			database.storeChunkStack(world, stack);
		}
	}

	public void clearCache(final Location center, final int cacheRange) {
		final int min_x = center.x - cacheRange;
		final int max_x = center.x + cacheRange;
		final int min_z = center.z - cacheRange;
		final int max_z = center.z + cacheRange;

		final List<ChunkStack> toRemove = new ArrayList<>();
		for (final ChunkStack chunkStack : cache.getChunkStacks()) {
			if (!MathUtil.isBetween(chunkStack.getX(), min_x, max_x) || !MathUtil.isBetween(chunkStack.getZ(), min_z, max_z)) {
				toRemove.add(chunkStack);
			}
		}
		for (final ChunkStack chunkStack : toRemove) {
			cache.removeChunkStackAt(chunkStack.getX(), chunkStack.getZ());

		}
		//System.out.println("removed " + toRemove.size() + " chunkstacks from cache, new size: " + cache.getChunkStacks().size());
	}
}
