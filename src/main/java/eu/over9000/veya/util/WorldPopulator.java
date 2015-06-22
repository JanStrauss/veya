package eu.over9000.veya.util;

import java.util.Random;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;

/**
 * Created by Jan on 22.06.2015.
 */
public class WorldPopulator {

	private static final int TREE_ATTEMPT_COUNT = 128;
	public static final int TREE_CHANCE = 50;

	public static void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {
		//System.out.println("POPULATOR CALLED FOR " + chunkX + "," + chunkZ);

		for (int attemp = 0; attemp < TREE_ATTEMPT_COUNT; attemp++) {
			if (random.nextInt(100) < TREE_CHANCE) {
				final int worldX = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
				final int worldZ = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);

				final int maxY = world.getHighestYAt(worldX, worldZ);

				if (maxY < WorldGenerator.SEALEVEL) {
					return;
				}

				if (world.getBlockAt(worldX, maxY, worldZ) != BlockType.GRASS) {
					return;
				}

				plantTree(world, worldX, maxY, worldZ);

			}
		}
	}

	private static void plantTree(final World world, final int xRoot, final int yRoot, final int zRoot) {
		for (int i = 0; i < 5; i++) {
			world.setBlockAtIfAir(xRoot, yRoot + i, zRoot, BlockType.WOOD);
		}
		for (int l = 0; l < 2; l++) {
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						world.setBlockAtIfAir(x, yRoot + 2 + l, z, BlockType.LEAVES);
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					world.setBlockAtIfAir(x, yRoot + 4, z, BlockType.LEAVES);
				}
			}
		}
		world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot - 1, yRoot + 5, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot + 1, yRoot + 5, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot - 1, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot + 1, BlockType.LEAVES);

	}
}
