package eu.over9000.veya.model.physic;

/**
 * Created by Jan on 26.06.2015.
 */
public class Gravity {

	public static void integrate(final State state, final float dt) {
		final Derivative a = evaluate(state, 0.0f, new Derivative());
		final Derivative b = evaluate(state, dt * 0.5f, a);
		final Derivative c = evaluate(state, dt * 0.5f, b);
		final Derivative d = evaluate(state, dt, c);

		final float dydt = 1.0f / 6.0f * (a.dy + 2.0f * (b.dy + c.dy) + d.dy);
		final float dvdt = 1.0f / 6.0f * (a.dv + 2.0f * (b.dv + c.dv) + d.dv);

		state.y = state.y + dydt * dt;
		state.v = state.v + dvdt * dt;
	}

	private static Derivative evaluate(final State initial, final float dt, final Derivative d) {
		final State state = new State();
		state.y = initial.y + d.dy * dt;
		state.v = initial.v + d.dv * dt;

		final Derivative output = new Derivative();
		output.dy = state.v;
		output.dv = acceleration(state);
		return output;
	}

	private static float acceleration(final State state) {
		final float k = 0.4f;
		final float b = 1f;
		return -k * state.y - b * state.v;
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

	public static class Derivative {
		public float dy;      // dy/dt = velocity
		public float dv;      // dv/dt = acceleration
	}
}
