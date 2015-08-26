/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
	
	public static boolean nextTo(final int a, final int b) {
		return a == b || a - 1 == b || a + 1 == b;
	}
}