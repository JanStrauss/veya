package eu.over9000.veya.generation.populators;

import java.util.Random;

import eu.over9000.veya.generation.WorldPopulator;
import eu.over9000.veya.model.world.BlockType;
import eu.over9000.veya.model.world.Chunk;
import eu.over9000.veya.model.world.World;

/**
 * Created by Jan on 23.06.2015.
 */
public class TreePopulator implements IPopulator {
	private static final int BASE_TREE_ATTEMPTS = 16;
	public static final int BASE_TREE_CHANCE = 20;
	private static final int LARGE_TREE_CHANCE = 20;

	public void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {

		final int chunkChance = BASE_TREE_CHANCE + random.nextInt(10);
		final int chunkAttempts = BASE_TREE_ATTEMPTS + random.nextInt(16);

		for (int attempt = 0; attempt < chunkAttempts; attempt++) {
			if (random.nextInt(100) < chunkChance) {
				final int worldX = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
				final int worldZ = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);

				final int maxY = world.getHighestYAt(worldX, worldZ);

				if (world.getBlockAt(worldX, maxY, worldZ) != BlockType.GRASS) {
					continue;
				}

				if (random.nextInt(100) < LARGE_TREE_CHANCE) {
					plantLargeTree(world, random, worldX, maxY + 1, worldZ);
				} else {
					plantDefaultTree(world, random, worldX, maxY + 1, worldZ);
				}

			}
		}
	}

	private static void plantLargeTree(final World world, final Random random, final int xRoot, final int yRoot, final int zRoot) {
		final int numCrowns = 1 + random.nextInt(3);
		final int height = 5 + random.nextInt(4);
		final int trunkRadius = 4 + random.nextInt(2);

		world.setBlockAt(xRoot, yRoot - 1, zRoot, BlockType.DIRT);
		for (int y = 0; y < height; y++) {
			world.setBlockAtIfAir(xRoot, yRoot + y, zRoot, BlockType.WOOD);
		}

		final int yTrunkTop = yRoot + height;
		WorldPopulator.placeRndSphere(world, random, xRoot, yTrunkTop, zRoot, trunkRadius, BlockType.LEAVES, blockType -> blockType == null);

		for (int crown = 0; crown < numCrowns; crown++) {
			final int crownRadius = 3 + random.nextInt(trunkRadius - 3);

			final int crownX = random.nextInt(7) - 3;
			final int crownY = random.nextInt(5) - 1;
			final int crownZ = random.nextInt(7) - 3;

			WorldPopulator.fillLine(world, xRoot + crownX, yTrunkTop + crownY, zRoot + crownZ, xRoot, yTrunkTop, zRoot, BlockType.WOOD);
			WorldPopulator.placeRndSphere(world, random, xRoot + crownX, yTrunkTop + crownY, zRoot + crownZ, crownRadius, BlockType.LEAVES, blockType -> blockType == null);
		}

	}

	private static void plantDefaultTree(final World world, final Random random, final int xRoot, final int yRoot, final int zRoot) {
		world.setBlockAt(xRoot, yRoot - 1, zRoot, BlockType.DIRT);

		final int height = 5 + random.nextInt(2);

		for (int i = 0; i < height; i++) {
			world.setBlockAtIfAir(xRoot, yRoot + i, zRoot, BlockType.WOOD);
		}
		for (int l = 0; l < 2; l++) {
			final int y = yRoot + height - 3 + l;
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						if (Math.abs(x - xRoot) + Math.abs(z - zRoot) == 4) {
							WorldPopulator.setBlockWithChance(world, x, y, z, BlockType.LEAVES, random, 0.75f);
						} else {
							world.setBlockAtIfAir(x, y, z, BlockType.LEAVES);
						}
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					world.setBlockAtIfAir(x, yRoot + height - 1, z, BlockType.LEAVES);
				}
			}
		}
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot - 1, yRoot + height, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot + 1, yRoot + height, zRoot, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot - 1, BlockType.LEAVES);
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot + 1, BlockType.LEAVES);

	}
}
