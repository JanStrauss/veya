/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
		//System.out.println("CACHE SET " + chunkX + "," + chunkZ);

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
