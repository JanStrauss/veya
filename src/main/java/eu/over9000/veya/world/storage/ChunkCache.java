package eu.over9000.veya.world.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkCache {

	private final Map<Integer, Map<Integer, ChunkStack>> xMap = new ConcurrentHashMap<>();

	private final Set<ChunkStack> chunkStackSet = new HashSet<>();

	public Collection<ChunkStack> getChunkStacks() {
		return this.chunkStackSet;
	}

	public ChunkStack getChunkStackAt(final int chunkX, final int chunkZ) {
		final Map<Integer, ChunkStack> zMap = this.xMap.get(chunkX);
		if (zMap == null) {
			return null;
		}

		return zMap.get(chunkZ);
	}

	public void setChunkStackAt(final int chunkX, final int chunkZ, final ChunkStack chunkStack) {
		Map<Integer, ChunkStack> zMap = this.xMap.get(chunkX);
		if (zMap == null) {
			zMap = new ConcurrentHashMap<>();
			this.xMap.put(chunkX, zMap);
		}

		zMap.put(chunkZ, chunkStack);
		this.chunkStackSet.add(chunkStack);
	}

	public void removeChunkStackAt(final int chunkX, final int chunkZ) {
		final Map<Integer, ChunkStack> zMap = this.xMap.get(chunkX);
		if (zMap == null) {
			return;
		}

		final ChunkStack stack = zMap.remove(chunkZ);

		if (stack != null) {
			chunkStackSet.remove(stack);
		}

	}
}
