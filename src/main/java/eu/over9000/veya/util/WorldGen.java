package eu.over9000.veya.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;

public class WorldGen {
	
	private static final int SEALEVEL = 64;
	
	private final World world;
	private final Random random;
	
	public WorldGen(final World world) {
		this.world = world;
		this.random = new Random(world.getSeed());
	}
	
	public void genChunksAt(final int chunkX, final int chunkZ) {
		System.out.println("GENERATOR CALLED FOR " + chunkX + "," + chunkZ);
		
		for (int x = chunkX * Chunk.CHUNK_SIZE; x < chunkX * Chunk.CHUNK_SIZE + Chunk.CHUNK_SIZE; x++) {
			for (int z = chunkZ * Chunk.CHUNK_SIZE; z < chunkZ * Chunk.CHUNK_SIZE + Chunk.CHUNK_SIZE; z++) {
				final List<Integer> topBlocks = new ArrayList<>();
				boolean createPre = false;
				
				for (int y = 0; y < World.MAX_WORLD_HEIGHT; y++) {
					
					if (this.genElevation(x, y, z)) {
						this.world.setBlockAt(x, y, z, BlockType.STONE);
						createPre = true;
					} else {
						if (y <= WorldGen.SEALEVEL) {
							this.world.setBlockAt(x, y, z, BlockType.WATER);
						}
						if (createPre) {
							
							topBlocks.add(y - 1);
							
						}
						createPre = false;
					}
				}
				Integer highest = 0;
				
				for (final Integer top : topBlocks) {
					
					this.fillTopWithDirtAndGrass(this.random, this.world, x, z, top);
					if (top > highest) {
						highest = top;
					}
				}
				
				// if (highest > WorldGen.SEALEVEL && this.random.nextInt(100) > 97) {
				// this.plantTree(x, highest + 1, z);
				// }
			}
		}
	}
	
	private void fillTopWithDirtAndGrass(final Random random, final World world, final int x, final int z, final int top) {
		if (top >= WorldGen.SEALEVEL) {
			world.setBlockAt(x, top, z, BlockType.GRASS);
		} else {
			world.setBlockAt(x, top, z, BlockType.DIRT);
		}
		
		final int dirtHeight = 3 + random.nextInt(3);
		final int dirtLimit = top - dirtHeight;
		for (int y = top; y > dirtLimit; y--) {
			if (world.getBlockAt(x, y, z) != null) {
				if (world.getBlockAt(x, y, z) == BlockType.STONE) {
					world.setBlockAt(x, y, z, BlockType.DIRT);
				}
			}
		}
	}
	
	private void plantTree(final int xRoot, final int yRoot, final int zRoot) {
		for (int i = 0; i < 5; i++) {
			this.world.setBlockAtIfAir(xRoot, yRoot + i, zRoot, BlockType.WOOD);
		}
		for (int l = 0; l < 2; l++) {
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						this.world.setBlockAtIfAir(x, yRoot + 2 + l, z, BlockType.LEAVES);
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					this.world.setBlockAtIfAir(x, yRoot + 4, z, BlockType.LEAVES);
				}
			}
		}
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot - 1, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot + 1, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot - 1, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot + 1, BlockType.LEAVES);
		
	}
	
	public boolean genElevation(final int x, final int y, final int z) {
		if (y < 50) {
			return true;
		}
		
		final float x_calc = x;
		final float z_calc = z;
		final float y_calc = y;
		
		final float max_world_dim_size = 250F;
		
		final float base = this.fbm(x_calc / max_world_dim_size, y_calc / 255F, z_calc / max_world_dim_size, 6, 2, 0.5F);
		
		float factor = 100 * 0.25F;
		
		final float density;
		factor += 0.75 * 75F;
		density = Math.abs(base * factor);
		return density - y + 55F > 0F;
		
	}
	
	private float fbm(final float x, final float y, final float z, final int octaves, final float lacunarity, final float gain) {
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
