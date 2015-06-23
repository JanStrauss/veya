package eu.over9000.veya.generation.populators;

import java.util.EnumSet;
import java.util.Random;

import eu.over9000.veya.generation.WorldGenerator;
import eu.over9000.veya.generation.WorldPopulator;
import eu.over9000.veya.model.world.BlockType;
import eu.over9000.veya.model.world.Chunk;
import eu.over9000.veya.model.world.World;

/**
 * Created by Jan on 23.06.2015.
 */
public class SandPopulator implements IPopulator {
	@Override
	public void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {

		final EnumSet<BlockType> valid = EnumSet.of(BlockType.DIRT, BlockType.STONE, BlockType.GRASS);

		final int chunkAttempts = random.nextInt(8);

		for (int attempt = 0; attempt < chunkAttempts; attempt++) {

			final int worldX = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
			final int worldZ = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);
			final int worldY = world.getHighestYAt(worldX, worldZ);
			if (worldY <= WorldGenerator.SEALEVEL) {
				WorldPopulator.placeRndSphere(world, random, worldX, worldY, worldZ, 6 + random.nextInt(6), BlockType.SAND, valid::contains);
			}
		}
	}
}
