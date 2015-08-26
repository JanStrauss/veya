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

package eu.over9000.veya.world.generation.populators;

import java.util.EnumSet;
import java.util.Random;

import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;
import eu.over9000.veya.world.generation.ChunkGenerator;
import eu.over9000.veya.world.generation.ChunkPopulator;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

/**
 * Created by Jan on 23.06.2015.
 */
public class SandPopulator implements IPopulator {
	@Override
	public void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {

		final EnumSet<BlockType> valid = EnumSet.of(BlockType.DIRT, BlockType.STONE, BlockType.GRASS);

		final int chunkAttempts = random.nextInt(5);

		for (int attempt = 0; attempt < chunkAttempts; attempt++) {

			final int worldX = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
			final int worldZ = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);
			final int worldY = world.getHighestYAt(worldX, worldZ, ChunkRequestLevel.GENERATOR);
			if (worldY <= ChunkGenerator.SEALEVEL) {
				ChunkPopulator.placeRndSphere(world, random, worldX, worldY, worldZ, 6 + random.nextInt(6), BlockType.SAND, valid::contains);
			}
		}
	}
}
