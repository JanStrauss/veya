/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
