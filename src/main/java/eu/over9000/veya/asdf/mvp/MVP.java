package eu.over9000.veya.asdf.mvp;

import java.nio.ByteBuffer;

import eu.over9000.veya.asdf.buffer.UBO;

public class MVP extends UBO {
	
	private static final int P_OFFSET = 0;
	private static final int MV_OFFSET = 16;
	
	private final float[] p = new float[16];
	private final float[] mv = new float[16];
	
	private final ByteBuffer buffer;
	
	/**
	 * @return the p
	 */
	public float[] getP() {
		return this.p;
	}
	
	/**
	 * @return the mv
	 */
	public float[] getMv() {
		return this.mv;
	}
	
	public MVP(final int binding, final int width, final int height) {
		
		super(binding, 32 * Float.SIZE / Byte.SIZE);
		
		this.buffer = this.getByteBuffer();
		
		for (int i = 0; i < 4; i++) {
			this.p[i * 5] = 1.0f;
		}
		
		for (int i = 0; i < 4; i++) {
			this.mv[i * 5] = 1.0f;
		}
		
	}
	
	public final void setPerspective(final float fovy, final float aspect, final float zNear, final float zFar) {
		final double f = 1.0 / Math.tan(Math.toRadians(fovy / 2.0));
		
		this.p[0] = (float) (f / aspect);
		this.p[5] = (float) f;
		this.p[10] = (zFar + zNear) / (zNear - zFar);
		this.p[11] = -1;
		this.p[14] = 2 * zFar * zNear / (zNear - zFar);
		
		for (int i = 0; i < this.p.length; i++) {
			this.buffer.putFloat((MVP.P_OFFSET + i) * 4, this.p[i]);
		}
		
		this.markDirty();
		
	}
	
	public final void setLookAt(final float eyeX, final float eyeY, final float eyeZ, final float centerX, final float centerY, final float centerZ, final float upX, final float upY, final float upZ) {
		
		final float[] F = this.sub(new float[] { centerX, centerY, centerZ }, new float[] { eyeX, eyeY, eyeZ });
		final float[] f = this.div(F, this.len(F));
		
		final float[] UP = new float[] { upX, upY, upZ };
		final float[] up = this.div(UP, this.len(UP));
		
		final float[] S = this.cross(f, up);
		final float[] s = this.div(S, this.len(S));
		
		final float[] u = this.cross(s, f);
		
		this.mv[0] = s[0];
		this.mv[1] = u[0];
		this.mv[2] = -f[0];
		this.mv[3] = 0;
		this.mv[4] = s[1];
		this.mv[5] = u[1];
		this.mv[6] = -f[1];
		this.mv[7] = 0;
		this.mv[8] = s[2];
		this.mv[9] = u[2];
		this.mv[10] = -f[2];
		this.mv[11] = 0;
		this.mv[12] = s[0] * -eyeX + s[1] * -eyeY + s[2] * -eyeZ;
		this.mv[13] = u[0] * -eyeX + u[1] * -eyeY + u[2] * -eyeZ;
		this.mv[14] = -f[0] * -eyeX - f[1] * -eyeY - f[2] * -eyeZ;
		this.mv[15] = 1;
		
		for (int i = 0; i < this.mv.length; i++) {
			this.buffer.putFloat((MVP.MV_OFFSET + i) * 4, this.mv[i]);
		}
		
		this.markDirty();
		
	}
	
	private final float[] sub(final float[] a, final float[] b) {
		
		final float[] result = new float[Math.min(a.length, b.length)];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = a[i] - b[i];
		}
		
		return result;
		
	}
	
	private final float len(final float[] a) {
		
		float result = 0;
		
		for (final float element : a) {
			result += element * element;
		}
		
		return (float) Math.sqrt(result);
		
	}
	
	private final float[] div(final float[] a, final float s) {
		
		final float[] result = new float[a.length];
		
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] / s;
		}
		
		return result;
		
	}
	
	private final float[] cross(final float[] a, final float[] b) {
		
		final float[] result = new float[3];
		
		result[0] = a[1] * b[2] - a[2] * b[1];
		result[1] = a[2] * b[0] - a[0] * b[2];
		result[2] = a[0] * b[1] - a[1] * b[0];
		
		return result;
		
	}
	
}
