package eu.over9000.veya.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import eu.over9000.veya.data.Chunk;

public class ChunkMap {

	private final Map<Integer, Map<Integer, Map<Integer, Chunk>>> xMap = new ConcurrentHashMap<>();

	private final Map<Integer, Map<Integer, ChunkState>> xChunkStateMap = new ConcurrentHashMap<>();

	private final Set<Chunk> chunkSet = new HashSet<>();

	public Collection<Chunk> getChunks() {
		return this.chunkSet;
	}

	public ChunkState getChunkState(final int chunkX, final int chunkZ) {
		Map<Integer, ChunkState> zGenMap = this.xChunkStateMap.get(chunkX);
		if (zGenMap == null) {
			zGenMap = new ConcurrentHashMap<>();
			this.xChunkStateMap.put(chunkX, zGenMap);
		}

		ChunkState state = zGenMap.get(chunkZ);

		if (state == null) {
			state = new ChunkState();
			zGenMap.put(chunkZ, state);

		}
		return state;
	}

	public Chunk getChunkAt(final int chunkX, final int chunkY, final int chunkZ) {
		final Map<Integer, Map<Integer, Chunk>> zMap = this.xMap.get(chunkX);
		if (zMap == null) {
			return null;
		}

		final Map<Integer, Chunk> yMap = zMap.get(chunkZ);

		if (yMap == null) {
			return null;
		}

		return yMap.get(chunkY);
	}

	public void setChunkAt(final int chunkX, final int chunkY, final int chunkZ, final Chunk chunk) {
		Map<Integer, Map<Integer, Chunk>> zMap = this.xMap.get(chunkX);
		if (zMap == null) {
			zMap = new ConcurrentHashMap<>();
			this.xMap.put(chunkX, zMap);
		}

		Map<Integer, Chunk> yMap = zMap.get(chunkZ);
		if (yMap == null) {
			yMap = new ConcurrentHashMap<>();
			zMap.put(chunkZ, yMap);
		}

		yMap.put(chunkY, chunk);
		this.chunkSet.add(chunk);
	}

	public static class ChunkState {
		public boolean generated = false;
		public boolean populated = false;
	}
}
