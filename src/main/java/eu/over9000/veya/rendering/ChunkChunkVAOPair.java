package eu.over9000.veya.rendering;

import java.util.Objects;

import eu.over9000.veya.world.Chunk;

/**
 * Created by Jan on 03.07.2015.
 */
public class ChunkChunkVAOPair {
	private final Chunk chunk;
	private final ChunkVAO chunkVAO;

	public ChunkChunkVAOPair(final Chunk chunk, final ChunkVAO chunkVAO) {
		this.chunk = chunk;
		this.chunkVAO = chunkVAO;
	}

	public Chunk getChunk() {
		return this.chunk;
	}

	public ChunkVAO getChunkVAO() {
		return this.chunkVAO;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ChunkChunkVAOPair that = (ChunkChunkVAOPair) o;
		return Objects.equals(chunk, that.chunk);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chunk);
	}
}
