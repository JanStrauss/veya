package eu.over9000.veya.util;

import java.util.Random;

public class MathUtil {
	public static boolean isBetween(final int value, final int min, final int max) {
		return min <= value && value <= max;
	}

	public static int randomBetween(final Random random, final int from, final int to) {
		return from + random.nextInt(to - from);
	}

	public static float scale(final float valueIn, final float baseMin, final float baseMax, final float limitMin, final float limitMax) {
		return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
	}

}
