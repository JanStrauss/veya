package eu.over9000.veya.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;

public class WorldGenerator {

	public static final int SEALEVEL = 64;

	public static List<Chunk> genChunksAt(World world, Random random, final int chunkX, final int chunkZ) {
		BlockType[][][] rawChunkStack = new BlockType[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][World.MAX_WORLD_HEIGHT];

		//System.out.println("GENERATOR CALLED FOR " + chunkX + "," + chunkZ);

		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
				final List<Integer> topBlocks = new ArrayList<>();
				boolean createPre = false;

				for (int y = 0; y < World.MAX_WORLD_HEIGHT; y++) {

					if (genElevation(x + chunkX * Chunk.CHUNK_SIZE, y, z + chunkZ * Chunk.CHUNK_SIZE)) {
						rawChunkStack[x][z][y] = BlockType.STONE;
						createPre = true;
					} else {
						if (y <= WorldGenerator.SEALEVEL) {
							rawChunkStack[x][z][y] = BlockType.WATER;
						}
						if (createPre) {

							topBlocks.add(y - 1);

						}
						createPre = false;
					}
				}

				for (final Integer top : topBlocks) {
					fillTopWithDirtAndGrass(random, rawChunkStack, x, z, top);
				}

			}
		}

		return buildChunks(world, chunkX, chunkZ, rawChunkStack);
	}

	private static List<Chunk> buildChunks(final World world, final int chunkX, final int chunkZ, final BlockType[][][] rawChunkStack) {
		ArrayList<Chunk> chunks = new ArrayList<>(World.MAX_WORLD_HEIGHT_IN_CHUNKS);

		for (int chunkY = 0; chunkY < World.MAX_WORLD_HEIGHT_IN_CHUNKS; chunkY++) {
			final Chunk chunk = new Chunk(world, chunkX, chunkY, chunkZ);

			for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
					int baseY = chunkY * Chunk.CHUNK_SIZE;
					for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
						chunk.setBlockAt(x, y, z, rawChunkStack[x][z][baseY + y]);
					}
				}
			}

			chunks.add(chunk);
		}
		return chunks;
	}

	private static void fillTopWithDirtAndGrass(final Random random, final BlockType[][][] rawChunkStack, final int x, final int z, final int top) {
		if (top >= WorldGenerator.SEALEVEL) {
			rawChunkStack[x][z][top] = BlockType.GRASS;
		} else {
			rawChunkStack[x][z][top] = BlockType.DIRT;
		}

		final int dirtHeight = 3 + random.nextInt(3);
		final int dirtLimit = top - dirtHeight;
		for (int y = top; y > dirtLimit; y--) {
			if (rawChunkStack[x][z][y] != null) {
				if (rawChunkStack[x][z][y] == BlockType.STONE) {
					rawChunkStack[x][z][y] = BlockType.DIRT;
				}
			}
		}
	}

	public static boolean genElevation(final int x, final int y, final int z) {
		if (y < 50) {
			return true;
		}

		final float x_calc = x;
		final float z_calc = z;
		final float y_calc = y;

		final float max_world_dim_size = 250F;

		final float base = fbm(x_calc / max_world_dim_size, y_calc / 255F, z_calc / max_world_dim_size, 6, 2, 0.5F);

		float factor = 100 * 0.25F;

		final float density;
		factor += 0.75 * 75F;
		density = Math.abs(base * factor);
		return density - y + 55F > 0F;

	}

	private static float fbm(final float x, final float y, final float z, final int octaves, final float lacunarity, final float gain) {
		// for each pixel, get the value
		float total = 0.0F;
		float frequency = 1.0F;
		float amplitude = gain;

		for (int i = 0; i < octaves; i++) {
			total += SimplexNoise.noise(x * frequency, y * frequency, z * frequency) * amplitude;
			frequency *= lacunarity;
			amplitude *= gain;
		}
		return total;
	}
}
