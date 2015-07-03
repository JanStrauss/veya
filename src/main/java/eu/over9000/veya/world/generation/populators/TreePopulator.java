package eu.over9000.veya.world.generation.populators;

import eu.over9000.veya.util.Location3D;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;
import eu.over9000.veya.world.generation.ChunkPopulator;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

import java.util.Random;

/**
 * Created by Jan on 23.06.2015.
 */
public class TreePopulator implements IPopulator {
	private static final int BASE_TREE_ATTEMPTS = 16;
	private static final int BASE_TREE_CHANCE = 20;
	private static final int SPECIAL_TREE_CHANGE = 20;
	private static final int SPRUCE_TREE_CHANCE = 33;
	private static final int OAK_TREE_CHANCE = 85;

	public void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {

		final int chunkChance = BASE_TREE_CHANCE + random.nextInt(10);
		final int chunkAttempts = BASE_TREE_ATTEMPTS + random.nextInt(16);

		for (int attempt = 0; attempt < chunkAttempts; attempt++) {
			if (random.nextInt(100) < chunkChance) {
				final int worldX = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
				final int worldZ = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);

				final int maxY = world.getHighestYAt(worldX, worldZ, ChunkRequestLevel.GENERATOR);

				if (world.getBlockAt(worldX, maxY, worldZ) != BlockType.GRASS) {
					continue;
				}

				final Location3D rootLocation = new Location3D(worldX, maxY + 1, worldZ);

				if (random.nextInt(100) < SPECIAL_TREE_CHANGE) {
					if (random.nextInt(100) < SPRUCE_TREE_CHANCE) {
						plantSpruceTree(world, random, rootLocation);
					} else {
						plantLargeTree(world, random, rootLocation);
					}
				} else {
					plantDefaultTree(world, random, rootLocation);
				}

			}
		}
	}

	private static void plantLargeTree(final World world, final Random random, final Location3D rootLocation) {
		final int numCrowns = 1 + random.nextInt(3);
		final int height = 5 + random.nextInt(4);
		final int trunkRadius = 4 + random.nextInt(2);

		world.setBlockAt(rootLocation.x, rootLocation.y - 1, rootLocation.z, BlockType.DIRT, ChunkRequestLevel.GENERATOR, false);
		for (int y = 0; y < height; y++) {
			world.setBlockAtIfAir(rootLocation.x, rootLocation.y + y, rootLocation.z, BlockType.LOG_OAK, ChunkRequestLevel.GENERATOR, true);
		}

		final int yTrunkTop = rootLocation.y + height;
		ChunkPopulator.placeRndSphere(world, random, rootLocation.x, yTrunkTop, rootLocation.z, trunkRadius, BlockType.LEAVES_DEFAULT, blockType -> blockType == null);

		for (int crown = 0; crown < numCrowns; crown++) {
			final int crownRadius = 3 + random.nextInt(trunkRadius - 3);

			final int crownX = random.nextInt(7) - 3;
			final int crownY = random.nextInt(5) - 1;
			final int crownZ = random.nextInt(7) - 3;

			ChunkPopulator.fillLine(world, rootLocation.x + crownX, yTrunkTop + crownY, rootLocation.z + crownZ, rootLocation.x, yTrunkTop, rootLocation.z, BlockType.LOG_SPRUCE);
			ChunkPopulator.placeRndSphere(world, random, rootLocation.x + crownX, yTrunkTop + crownY, rootLocation.z + crownZ, crownRadius, BlockType.LEAVES_DEFAULT, blockType -> blockType == null);
		}

	}

	private static void plantDefaultTree(final World world, final Random random, final Location3D rootLocation) {
		world.setBlockAt(rootLocation.x, rootLocation.y - 1, rootLocation.z, BlockType.DIRT, ChunkRequestLevel.GENERATOR, false);

		final int height = 5 + random.nextInt(2);
		final BlockType logType = random.nextInt(100) < OAK_TREE_CHANCE ? BlockType.LOG_OAK : BlockType.LOG_BIRCH;

		for (int i = 0; i < height; i++) {
			world.setBlockAtIfAir(rootLocation.x, rootLocation.y + i, rootLocation.z, logType, ChunkRequestLevel.GENERATOR, true);
		}
		for (int l = 0; l < 2; l++) {
			final int y = rootLocation.y + height - 3 + l;
			for (int x = rootLocation.x - 2; x <= rootLocation.x + 2; x++) {
				for (int z = rootLocation.z - 2; z <= rootLocation.z + 2; z++) {
					if (x != rootLocation.x || z != rootLocation.z) {
						if (Math.abs(x - rootLocation.x) + Math.abs(z - rootLocation.z) == 4) {
							ChunkPopulator.setBlockWithChance(world, x, y, z, BlockType.LEAVES_DEFAULT, random, 0.75f);
						} else {
							world.setBlockAtIfAir(x, y, z, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
						}
					}
				}
			}
		}
		for (int x = rootLocation.x - 1; x <= rootLocation.x + 1; x++) {
			for (int z = rootLocation.z - 1; z <= rootLocation.z + 1; z++) {
				if (x != rootLocation.x || z != rootLocation.z) {
					world.setBlockAtIfAir(x, rootLocation.y + height - 1, z, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
				}
			}
		}
		world.setBlockAtIfAir(rootLocation.x, rootLocation.y + height, rootLocation.z, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
		world.setBlockAtIfAir(rootLocation.x - 1, rootLocation.y + height, rootLocation.z, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
		world.setBlockAtIfAir(rootLocation.x + 1, rootLocation.y + height, rootLocation.z, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
		world.setBlockAtIfAir(rootLocation.x, rootLocation.y + height, rootLocation.z - 1, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);
		world.setBlockAtIfAir(rootLocation.x, rootLocation.y + height, rootLocation.z + 1, BlockType.LEAVES_DEFAULT, ChunkRequestLevel.GENERATOR, true);

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
			world.setBlockAt(rootLocation.x, rootLocation.y + logY, rootLocation.z, BlockType.LOG_SPRUCE, ChunkRequestLevel.GENERATOR, true);
		}

		// leaves
		for (int yOffset = 0; yOffset <= leavesBottomLimit; yOffset++) {
			final int yCurrent = rootLocation.y + height - yOffset;

			for (int xCurrent = rootLocation.x - leavesRange; xCurrent <= rootLocation.x + leavesRange; xCurrent++) {
				final int xDist = xCurrent - rootLocation.x;

				for (int zCurrent = rootLocation.z - leavesRange; zCurrent <= rootLocation.z + leavesRange; zCurrent++) {
					final int zDist = zCurrent - rootLocation.z;

					if (Math.abs(xDist) != leavesRange || Math.abs(zDist) != leavesRange || leavesRange <= 0) {
						world.setBlockAtIfAir(xCurrent, yCurrent, zCurrent, BlockType.LEAVES_SPRUCE, ChunkRequestLevel.GENERATOR, true);
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
