package eu.over9000.veya.generation.populators;

import java.util.Random;

import eu.over9000.veya.generation.WorldPopulator;
import eu.over9000.veya.model.world.BlockType;
import eu.over9000.veya.model.world.Chunk;
import eu.over9000.veya.model.world.World;
import eu.over9000.veya.util.Location3D;

/**
 * Created by Jan on 23.06.2015.
 */
public class TreePopulator implements IPopulator {
	private static final int BASE_TREE_ATTEMPTS = 16;
	public static final int BASE_TREE_CHANCE = 20;
	private static final int SPECIAL_TREE_CHANGE = 20;
	private static final int SPRUCE_TREE_CHANCE = 33;
	public static final int OAK_TREE_CHANCE = 85;

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

				final Location3D rootLocation = new Location3D(worldX, maxY + 1, worldZ);

				if (random.nextInt(100) < SPECIAL_TREE_CHANGE) {
					if (random.nextInt(100) < SPRUCE_TREE_CHANCE) {
						plantSpruceTree(world, random, rootLocation);
					} else {
						plantLargeTree(world, random, worldX, maxY + 1, worldZ);
					}
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
			world.setBlockAtIfAir(xRoot, yRoot + y, zRoot, BlockType.LOG_OAK);
		}

		final int yTrunkTop = yRoot + height;
		WorldPopulator.placeRndSphere(world, random, xRoot, yTrunkTop, zRoot, trunkRadius, BlockType.LEAVES_DEFAULT, blockType -> blockType == null);

		for (int crown = 0; crown < numCrowns; crown++) {
			final int crownRadius = 3 + random.nextInt(trunkRadius - 3);

			final int crownX = random.nextInt(7) - 3;
			final int crownY = random.nextInt(5) - 1;
			final int crownZ = random.nextInt(7) - 3;

			WorldPopulator.fillLine(world, xRoot + crownX, yTrunkTop + crownY, zRoot + crownZ, xRoot, yTrunkTop, zRoot, BlockType.LOG_SPRUCE);
			WorldPopulator.placeRndSphere(world, random, xRoot + crownX, yTrunkTop + crownY, zRoot + crownZ, crownRadius, BlockType.LEAVES_DEFAULT, blockType -> blockType == null);
		}

	}

	private static void plantDefaultTree(final World world, final Random random, final int xRoot, final int yRoot, final int zRoot) {
		world.setBlockAt(xRoot, yRoot - 1, zRoot, BlockType.DIRT);

		final int height = 5 + random.nextInt(2);
		final BlockType logType = random.nextInt(100) < OAK_TREE_CHANCE ? BlockType.LOG_OAK : BlockType.LOG_BIRCH;

		for (int i = 0; i < height; i++) {
			world.setBlockAtIfAir(xRoot, yRoot + i, zRoot, logType);
		}
		for (int l = 0; l < 2; l++) {
			final int y = yRoot + height - 3 + l;
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						if (Math.abs(x - xRoot) + Math.abs(z - zRoot) == 4) {
							WorldPopulator.setBlockWithChance(world, x, y, z, BlockType.LEAVES_DEFAULT, random, 0.75f);
						} else {
							world.setBlockAtIfAir(x, y, z, BlockType.LEAVES_DEFAULT);
						}
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					world.setBlockAtIfAir(x, yRoot + height - 1, z, BlockType.LEAVES_DEFAULT);
				}
			}
		}
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot, BlockType.LEAVES_DEFAULT);
		world.setBlockAtIfAir(xRoot - 1, yRoot + height, zRoot, BlockType.LEAVES_DEFAULT);
		world.setBlockAtIfAir(xRoot + 1, yRoot + height, zRoot, BlockType.LEAVES_DEFAULT);
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot - 1, BlockType.LEAVES_DEFAULT);
		world.setBlockAtIfAir(xRoot, yRoot + height, zRoot + 1, BlockType.LEAVES_DEFAULT);

	}

	private static void plantSpruceTree(final World world, final Random random, final Location3D rootLocation) {
		final int height = random.nextInt(4) + 6;
		final int leavesGroundDist = 1 + random.nextInt(2);
		final int leavesBottomLimit = height - leavesGroundDist;
		final int maxSize = 2 + random.nextInt(2);

		int leavesRange = random.nextInt(2);
		int leavesRangeLimit = 1;
		byte leavesResetRange = 0;

		// trunk
		final int logOffset = random.nextInt(3);
		for (int logY = 0; logY < height - logOffset; logY++) {
			world.setBlockAt(rootLocation.x, rootLocation.y + logY, rootLocation.z, BlockType.LOG_SPRUCE);
		}

		// leaves
		for (int yOffset = 0; yOffset <= leavesBottomLimit; yOffset++) {
			final int yCurrent = rootLocation.y + height - yOffset;

			for (int xCurrent = rootLocation.x - leavesRange; xCurrent <= rootLocation.x + leavesRange; xCurrent++) {
				final int xDist = xCurrent - rootLocation.x;

				for (int zCurrent = rootLocation.z - leavesRange; zCurrent <= rootLocation.z + leavesRange; zCurrent++) {
					final int zDist = zCurrent - rootLocation.z;

					if (Math.abs(xDist) != leavesRange || Math.abs(zDist) != leavesRange || leavesRange <= 0) {
						world.setBlockAtIfAir(xCurrent, yCurrent, zCurrent, BlockType.LEAVES_SPRUCE);
					}
				}
			}

			if (leavesRange >= leavesRangeLimit) {
				leavesRange = leavesResetRange;
				leavesResetRange = 1;
				leavesRangeLimit++;

				if (leavesRangeLimit > maxSize) {
					leavesRangeLimit = maxSize;
				}
			} else {
				leavesRange++;
			}
		}

	}

}
