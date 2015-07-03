package eu.over9000.veya.world.generation;

import eu.over9000.veya.util.Location3D;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.World;
import eu.over9000.veya.world.generation.populators.*;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by Jan on 22.06.2015.
 */
public class ChunkPopulator {

	private static final List<IPopulator> populators = new ArrayList<>();

	static {
		populators.add(new OrePopulator());
		populators.add(new SandPopulator());
		populators.add(new GravelPopulator());
		populators.add(new TreePopulator());
	}

	public static void populateChunkStack(final World world, final Random random, final int chunkX, final int chunkZ) {
		for (final IPopulator populator : populators) {
			populator.populateChunkStack(world, random, chunkX, chunkZ);
		}
	}

	public static void setBlockWithChance(final World world, final int x, final int y, final int z, final BlockType block, final Random random, final float chance) {
		if (random.nextFloat() < chance) {
			world.setBlockAtIfAir(x, y, z, block, ChunkRequestLevel.GENERATOR, true);
		}
	}

	public static void fillLine(final World world, final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ, final BlockType block) {
		final int side_a = toX - fromX;
		final int side_b = toY - fromY;
		final int side_c = toZ - fromZ;

		final double length = Math.sqrt((side_a * side_a) + (side_b * side_b) + (side_c * side_c));
		final double mod = 1 / length;
		double i = 0;

		int pos_x;
		int pos_y;
		int pos_z;

		while (i <= 1) {
			pos_x = (int) Math.floor(fromX + i * (toX - fromX));
			pos_y = (int) Math.floor(fromY + i * (toY - fromY));
			pos_z = (int) Math.floor(fromZ + i * (toZ - fromZ));


			world.setBlockAt(pos_x, pos_y, pos_z, block, ChunkRequestLevel.GENERATOR, true);

			i = i + (mod);
		}
	}

	public static void placeRndSphere(final World world, final Random random, final int centerX, final int centerY, final int centerZ, final int crownRadius, final BlockType block, final Predicate<BlockType> condition) {
		final float rndOffset = random.nextFloat() * 0.25f;

		final Location3D crownCenter = new Location3D(0, 0, 0);
		final List<Location3D> locations = new ArrayList<>();
		for (int x = -crownRadius; x <= crownRadius; x++) {
			for (int y = -crownRadius; y <= crownRadius; y++) {
				for (int z = -crownRadius; z <= crownRadius; z++) {
					if (x * x + y * y + z * z < crownRadius * crownRadius) {
						locations.add(new Location3D(x, y, z, crownCenter));
					}
				}
			}
		}
		Collections.sort(locations);

		for (int i = 0; i < locations.size(); i++) {
			final Location3D location = locations.get(i);

			final float percent = (float) i / (float) locations.size();

			final BlockType current = world.getBlockAt(centerX + location.x, centerY + location.y, centerZ + location.z);
			if (condition.test(current)) {
				if (percent < 0.5f + rndOffset) {
					world.setBlockAt(centerX + location.x, centerY + location.y, centerZ + location.z, block, ChunkRequestLevel.GENERATOR, true);
				} else if (random.nextBoolean()) {
					world.setBlockAt(centerX + location.x, centerY + location.y, centerZ + location.z, block, ChunkRequestLevel.GENERATOR, true);
				}
			}
		}
	}
}
