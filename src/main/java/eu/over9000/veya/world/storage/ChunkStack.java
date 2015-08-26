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
