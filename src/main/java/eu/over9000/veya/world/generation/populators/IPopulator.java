package eu.over9000.veya.world.generation.populators;

import java.util.Random;

import eu.over9000.veya.world.World;

/**
 * Created by Jan on 23.06.2015.
 */
public interface IPopulator {
	void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ);
}
