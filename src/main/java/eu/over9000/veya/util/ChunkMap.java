package eu.over9000.veya.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import eu.over9000.veya.data.Chunk;

public class ChunkMap {
	
	private final Map<Integer, Map<Integer, Map<Integer, Chunk>>> xMap = new ConcurrentHashMap<>();
	
	private final Map<Integer, Map<Integer, Boolean>> xGenMap = new ConcurrentHashMap<>();
	
	private final Set<Chunk> chunkSet = new HashSet<>();
	
	public Collection<Chunk> getChunks() {
		return this.chunkSet;
	}
	
	public void markAsGenerated(final int chunkX, final int chunkZ) {
		Map<Integer, Boolean> zGenMap = this.xGenMap.get(chunkX);
		if (zGenMap == null) {
			zGenMap = new ConcurrentHashMap<>();
			this.xGenMap.put(chunkX, zGenMap);
		}
		
		zGenMap.put(chunkZ, true);
	}
	
	public boolean isGenerated(final int chunkX, final int chunkZ) {
		final Map<Integer, Boolean> zGenMap = this.xGenMap.get(chunkX);
		if (zGenMap == null) {
			return false;
		}
		
		final Boolean result = zGenMap.get(chunkZ);
		return result == null ? false : result;
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
}
