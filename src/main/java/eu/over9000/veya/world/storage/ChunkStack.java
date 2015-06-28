package eu.over9000.veya.world.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;

/**
 * Created by Jan on 28.06.2015.
 */
public class ChunkStack {
	private final Map<Integer, Chunk> stack = new ConcurrentHashMap<>();

	private final World world;
	private final int x;
	private final int z;
	private boolean populated = false;

	public ChunkStack(final World world, final int x, final int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public Chunk getChunkAt(int y) {
		return stack.get(y);
	}

	public void setChunkAt(final int chunkY, final Chunk chunk) {
		stack.put(chunkY, chunk);
	}

	public Chunk removeChunkAt(final int chunkY) {
		return stack.remove(chunkY);
	}

	public boolean isPopulated() {
		return populated;
	}

	public void setPopulated(final boolean populated) {
		this.populated = populated;
	}

	public int getZ() {
		return z;
	}

	public int getX() {
		return x;
	}

	public World getWorld() {
		return world;
	}

	public Collection<Chunk> getChunks() {
		return stack.values();
	}
}
