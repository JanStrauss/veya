/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.over9000.veya.util;

import eu.over9000.veya.Veya;

/**
 * Created by Jan on 26.06.2015.
 */
public class Gravity {

	public static void apply(final State state, final float dt) {
		final float acceleration = Veya.getMovementMultiplier() * -9.81f;
		final float old_v = state.v;
		state.v = state.v + acceleration * dt;
		state.y = state.y + (old_v + state.v) * 0.5f * dt;
	}

	public static class State {
		public float y;      // position
		public float v;      // velocity

		@Override
		public String toString() {
			return "State{" +
					"y=" + y +
					", v=" + v +
					'}';
		}
	}
}
