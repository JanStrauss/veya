package net.mschorn.eraseme.mvp;

import java.nio.ByteBuffer;

import net.mschorn.eraseme.buffer.UBO;

public class MVP extends UBO {
	
	private static final int P_OFFSET = 0;
	private static final int V_OFFSET = 16;
	private static final int M_OFFSET = 32;
	
	private final float[] p = new float[16];
	private final float[] v = new float[16];
	private final float[] m = new float[16];
	
	private final ByteBuffer buffer;
	
	public MVP(final int binding, final int width, final int height) {
		
		super(binding, 48 * Float.SIZE / Byte.SIZE);
		
		this.buffer = this.getByteBuffer();
		
		for (int i = 0; i < 4; i++) {
			this.p[i * 5] = 1.0f;
		}
		
		for (int i = 0; i < 4; i++) {
			this.v[i * 5] = 1.0f;
		}
		
		for (int i = 0; i < 4; i++) {
			this.m[i * 5] = 1.0f;
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
		
		this.v[0] = s[0];
		this.v[1] = u[0];
		this.v[2] = -f[0];
		this.v[3] = 0;
		this.v[4] = s[1];
		this.v[5] = u[1];
		this.v[6] = -f[1];
		this.v[7] = 0;
		this.v[8] = s[2];
		this.v[9] = u[2];
		this.v[10] = -f[2];
		this.v[11] = 0;
		this.v[12] = s[0] * -eyeX + s[1] * -eyeY + s[2] * -eyeZ;
		this.v[13] = u[0] * -eyeX + u[1] * -eyeY + u[2] * -eyeZ;
		this.v[14] = -f[0] * -eyeX - f[1] * -eyeY - f[2] * -eyeZ;
		this.v[15] = 1;
		
		for (int i = 0; i < this.v.length; i++) {
			this.buffer.putFloat((MVP.V_OFFSET + i) * 4, this.v[i]);
		}
		
		this.markDirty();
		
	}
	
	public final void setModelTranslation(final float transX, final float transY, final float transZ) {
		this.m[12] = transX;
		this.m[13] = transY;
		this.m[14] = transZ;
		
		for (int i = 0; i < this.m.length; i++) {
			this.buffer.putFloat((MVP.M_OFFSET + i) * 4, this.m[i]);
		}
		
		this.markDirty();
	}
	
	public void setModelScale(final float sX, final float sY, final float sZ) {
		this.m[0] = sX;
		this.m[5] = sY;
		this.m[10] = sZ;
		
		for (int i = 0; i < this.m.length; i++) {
			this.buffer.putFloat((MVP.M_OFFSET + i) * 4, this.m[i]);
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
