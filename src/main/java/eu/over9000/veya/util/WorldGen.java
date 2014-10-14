package eu.over9000.veya.util;

public class WorldGen {
	
	public static boolean genElevation(final int x, final int y, final int z) {
		if (y < 50) {
			return true;
		}
		
		final float x_calc = x;
		final float z_calc = z;
		final float y_calc = y;
		
		final float max_world_dim_size = 250F;
		
		final float base = WorldGen.fbm(x_calc / max_world_dim_size, y_calc / 255F, z_calc / max_world_dim_size, 6, 2, 0.5F);
		
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
