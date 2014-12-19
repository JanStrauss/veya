package eu.over9000.veya.util;

public class CoordinatesUtil {
	public static boolean isBetween(final int value, final int min, final int max) {
		return min <= value && value <= max;
	}
	
}
