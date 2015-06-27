package eu.over9000.veya.model.physic;

import eu.over9000.veya.Veya;

/**
 * Created by Jan on 26.06.2015.
 */
public class Gravity {

	public static void integrate(final State state, final float dt) {
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
