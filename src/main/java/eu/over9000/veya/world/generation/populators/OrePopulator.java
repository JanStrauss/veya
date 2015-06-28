package eu.over9000.veya.world.generation.populators;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;
import eu.over9000.veya.util.Location3D;
import eu.over9000.veya.util.MathUtil;

/**
 * Created by Jan on 27.06.2015.
 */
public class OrePopulator implements IPopulator {

	private class OreProperties {
		private final int rounds_limit;

		private final int height_limit_low;
		private final int height_limit_high;

		private final int size_limit_low;
		private final int size_limit_high;

		public OreProperties(final int rounds_limit, final int height_limit_low, final int height_limit_high, final int size_limit_low, final int size_limit_high) {
			this.height_limit_low = height_limit_low;
			this.height_limit_high = height_limit_high;
			this.rounds_limit = rounds_limit;
			this.size_limit_low = size_limit_low;
			this.size_limit_high = size_limit_high;
		}
	}

	private final Map<BlockType, OreProperties> properties = new HashMap<>();

	public OrePopulator() {
		properties.put(BlockType.COAL_ORE, new OreProperties(12, 3, 120, 5, 25));
		properties.put(BlockType.IRON_ORE, new OreProperties(4, 3, 64, 3, 15));
	}

	@Override
	public void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {
		for (final BlockType ore : properties.keySet()) {
			populateOre(world, random, chunkX, chunkZ, ore);
		}
	}

	private void populateOre(final World world, final Random random, final int chunkX, final int chunkZ, final BlockType oreBlock) {
		final OreProperties props = properties.get(oreBlock);
		for (int round = 0; round < props.rounds_limit; round++) {
			final int x = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkX);
			final int y = MathUtil.randomBetween(random, props.height_limit_low, props.height_limit_high);
			final int z = World.chunkToWorldCoordinate(random.nextInt(Chunk.CHUNK_SIZE), chunkZ);

			populateOreInternal(world, random, new Location3D(x, y, z), MathUtil.randomBetween(random, props.size_limit_low, props.size_limit_high), oreBlock);
		}
	}

	private void populateOreInternal(final World world, final Random random, final Location3D position, final int blockCount, final BlockType oreBlock) {
		final double rpi = random.nextDouble() * Math.PI;

		final double x1 = position.x + 8 + Math.sin(rpi) * blockCount / 8.0F;
		final double x2 = position.x + 8 - Math.sin(rpi) * blockCount / 8.0F;
		final double z1 = position.z + 8 + Math.cos(rpi) * blockCount / 8.0F;
		final double z2 = position.z + 8 - Math.cos(rpi) * blockCount / 8.0F;

		final double y1 = position.y + random.nextInt(3) + 2;
		final double y2 = position.y + random.nextInt(3) + 2;

		for (int i = 0; i <= blockCount; i++) {
			final double xPos = x1 + (x2 - x1) * i / blockCount;
			final double yPos = y1 + (y2 - y1) * i / blockCount;
			final double zPos = z1 + (z2 - z1) * i / blockCount;

			final double fuzz = random.nextDouble() * blockCount / 16.0D;
			final double fuzzXZ = (Math.sin((float) (i * Math.PI / blockCount)) + 1.0F) * fuzz + 1.0D;
			final double fuzzY = (Math.sin((float) (i * Math.PI / blockCount)) + 1.0F) * fuzz + 1.0D;

			final int xStart = (int) Math.floor(xPos - fuzzXZ / 2.0D);
			final int yStart = (int) Math.floor(yPos - fuzzY / 2.0D);
			final int zStart = (int) Math.floor(zPos - fuzzXZ / 2.0D);

			final int xEnd = (int) Math.floor(xPos + fuzzXZ / 2.0D);
			final int yEnd = (int) Math.floor(yPos + fuzzY / 2.0D);
			final int zEnd = (int) Math.floor(zPos + fuzzXZ / 2.0D);

			for (int x = xStart; x <= xEnd; x++) {
				final double xThresh = (x + 0.5D - xPos) / (fuzzXZ / 2.0D);
				if (xThresh * xThresh < 1.0D) {
					for (int y = yStart; y <= yEnd; y++) {
						final double yThresh = (y + 0.5D - yPos) / (fuzzY / 2.0D);
						if (xThresh * xThresh + yThresh * yThresh < 1.0D) {
							for (int z = zStart; z <= zEnd; z++) {
								final double zThresh = (z + 0.5D - zPos) / (fuzzXZ / 2.0D);
								if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0D) {
									if (world.getBlockAt(x, y, z) == BlockType.STONE) {
										world.setBlockAt(x, y, z, oreBlock);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

