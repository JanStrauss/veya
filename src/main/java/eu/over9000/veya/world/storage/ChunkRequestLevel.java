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

/**
 * Created by Jan on 28.06.2015.
 */
public enum ChunkRequestLevel {

	// CACHE => only check cache
	// DATABASE => check cache + db
	// GENERATOR -> check cache + db + generator
	// FULL => check cache + db + generator + populator

	CACHE, DATABASE, GENERATOR, POPULATED, NEIGHBORS_LOADED;

	public boolean includes(final ChunkRequestLevel other) {
		return this.ordinal() >= other.ordinal();
	}
}
