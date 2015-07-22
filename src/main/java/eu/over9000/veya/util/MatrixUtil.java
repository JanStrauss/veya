package eu.over9000.veya.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Jan on 22.07.2015.
 */
public class MatrixUtil {

	public static Matrix4f orthographic(final float left, final float right, final float bottom, final float top, final float near, final float far) {
		final Matrix4f matrix = new Matrix4f();

		matrix.m00 = (2.0f) / (right - left);
		matrix.m11 = (2.0f) / (top - bottom);
		matrix.m22 = -(2.0f) / (far - near);
		matrix.m30 = -(right + left) / (right - left);
		matrix.m31 = -(top + bottom) / (top - bottom);
		matrix.m32 = -(far + near) / (far - near);
		matrix.m33 = 1.0f;

		return matrix;
	}

	public static Matrix4f projection(final float fov, final float width, final float height, final float far, final float near) {
		final float aspectRatio = width / height;
		final Matrix4f projectionMatrix = new Matrix4f();
		final float y_scale = (float) (1.0f / Math.tan(Math.toRadians(fov / 2.0f)));
		final float x_scale = y_scale / aspectRatio;
		final float frustum_length = far - near;
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far + near) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -(2 * near * far / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}

	public static Matrix4f lookAtCenter(final Vector3f eye, final Vector3f center) {
		final Matrix4f matrix = new Matrix4f();

		center.setY(64);

		final Vector3f up = new Vector3f(.0f, 1.0f, .0f).normalise(null);

		final Vector3f front = Vector3f.sub(center, eye, null).normalise(null); // front
		final Vector3f side = Vector3f.cross(front, up, null).normalise(null); // side
		final Vector3f upCam = Vector3f.cross(front, side, null); // up in cam

		matrix.m00 = side.x;
		matrix.m10 = side.y;
		matrix.m20 = side.z;
		matrix.m01 = upCam.x;
		matrix.m11 = upCam.y;
		matrix.m21 = upCam.z;
		matrix.m02 = -front.x;
		matrix.m12 = -front.y;
		matrix.m22 = -front.z;
		matrix.m33 = 1f;

		return matrix.translate(eye.negate(null));
	}
}
