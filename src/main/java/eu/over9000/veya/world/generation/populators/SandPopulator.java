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
