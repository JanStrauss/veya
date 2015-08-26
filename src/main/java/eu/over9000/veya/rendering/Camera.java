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

package eu.over9000.veya.rendering;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.Veya;
import eu.over9000.veya.collision.AABB;
import eu.over9000.veya.collision.CollisionDetection;
import eu.over9000.veya.util.Gravity;
import eu.over9000.veya.util.Location;
import eu.over9000.veya.util.MatrixUtil;

public class Camera {
	private static final float CAMERA_OFFSET_SIDE = 0.25f;
	private static final float CAMERA_OFFSET_BOTTOM = 1.7f;
	private static final float CAMERA_OFFSET_TOP = 0.1f;

	private static final float PITCH_LIMIT = (float) (90 * Math.PI / 180);
	private static final float YAW_LIMIT = 2f * (float) Math.PI;

	private final Vector3f currentPosition;
	private final Vector3f nextPosition;
	private final Gravity.State state;

	private float yaw = 0;
	private float pitch = 0;

	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private final int viewMatrixLocation;
	private final int projectionMatrixLocation;
	private final int cameraPositionLocation;

	private boolean jumping = false;
	private boolean onGround = false;

	public Camera(final float posX, final float posY, final float posZ) {
		this.viewMatrixLocation = Veya.program_normal.getUniformLocation("viewMatrix");
		this.projectionMatrixLocation = Veya.program_normal.getUniformLocation("projectionMatrix");
		this.cameraPositionLocation = Veya.program_normal.getUniformLocation("cameraPosition");
		this.currentPosition = new Vector3f(posX, posY, posZ);
		this.nextPosition = new Vector3f(posX, posY, posZ);
		this.state = new Gravity.State();
		state.v = 0;
		state.y = posY;
	}

	public void updateCameraPosition() {
		GL20.glUniform3f(this.cameraPositionLocation, this.currentPosition.x, this.currentPosition.y, this.currentPosition.z);
	}

	public void updateViewMatrix() {
		final Matrix4f viewMatrix = new Matrix4f();

		viewMatrix.rotate(this.pitch, new Vector3f(1, 0, 0), viewMatrix);
		viewMatrix.rotate(this.yaw, new Vector3f(0, 1, 0), viewMatrix);
		viewMatrix.translate(this.currentPosition.negate(null), viewMatrix);

		viewMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.viewMatrixLocation, false, this.matrixBuffer);

	}

	public void updateProjectionMatrix(final int width, final int height) {
		final Matrix4f projectionMatrix = MatrixUtil.projection(Veya.FIELD_OF_VIEW, width, height, Veya.FAR_CLIPPING_PLANE, Veya.NEAR_CLIPPING_PLANE);

		projectionMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.projectionMatrixLocation, false, this.matrixBuffer);

		// System.out.println("updated projection matrix:");
		// System.out.println(this.projectionMatrix);

	}

	// moves the camera forward relative to its current rotation (yaw)
	public void tryWalkForward(final float distance) {
		this.nextPosition.x += distance * (float) Math.sin(this.yaw);
		this.nextPosition.z -= distance * (float) Math.cos(this.yaw);
	}

	// moves the camera backward relative to its current rotation (yaw)
	public void tryWalkBackwards(final float distance) {
		this.nextPosition.x -= distance * (float) Math.sin(this.yaw);
		this.nextPosition.z += distance * (float) Math.cos(this.yaw);
	}

	// strafes the camera left relitive to its current rotation (yaw)
	public void tryStrafeLeft(final float distance) {
		this.nextPosition.x += distance * (float) Math.sin(this.yaw - Math.toRadians(90));
		this.nextPosition.z -= distance * (float) Math.cos(this.yaw - Math.toRadians(90));
	}

	// strafes the camera right relitive to its current rotation (yaw)
	public void tryStrafeRight(final float distance) {
		this.nextPosition.x += distance * (float) Math.sin(this.yaw + Math.toRadians(90));
		this.nextPosition.z -= distance * (float) Math.cos(this.yaw + Math.toRadians(90));
	}

	// increment the camera's current yaw rotation
	public void yaw(final float amount) {
		this.yaw = (yaw + amount) % YAW_LIMIT;

	}

	// increment the camera's current yaw rotation
	public void pitch(final float amount) {
		// increment the pitch by the amount param
		this.pitch -= amount;
		if (this.pitch > PITCH_LIMIT) {
			this.pitch = (float) (90 * Math.PI / 180);
		}
		if (this.pitch < -PITCH_LIMIT) {
			this.pitch = (float) (-90 * Math.PI / 180);
		}

	}

	public void tryMoveUp(final float distance) {
		if (Veya.gravitySwitch) {
			if (onGround && !jumping) {
				this.jumping = true;
				this.state.v = 2.5f * Veya.getMovementMultiplier();
			}
		} else {
			this.nextPosition.y += distance;
		}
	}

	public void tryMoveDown(final float distance) {
		this.nextPosition.y -= distance;
	}

	public void performMove() {
		if (currentPosition.equals(nextPosition)) {
			return;
		}

		final boolean checkX = checkNewPositionSingleDim(buildAABB(nextPosition.x, currentPosition.y, currentPosition.z));
		final boolean checkY = checkNewPositionSingleDim(buildAABB(currentPosition.x, nextPosition.y, currentPosition.z));
		final boolean checkZ = checkNewPositionSingleDim(buildAABB(currentPosition.x, currentPosition.y, nextPosition.z));

		if (!checkX || !Veya.collisionSwitch) {
			this.currentPosition.x = nextPosition.x;
		}

		if (!checkY || !Veya.collisionSwitch) { // no collision
			this.currentPosition.y = nextPosition.y;
			onGround = false;
		} else { // collision
			this.state.v = 0;
			if (currentPosition.y > nextPosition.y) {
				jumping = false;
				onGround = true;
				Veya.gravitySwitch = true;
			}
		}
		if (!checkZ || !Veya.collisionSwitch) {
			this.currentPosition.z = nextPosition.z;
		}

		nextPosition.x = currentPosition.x;
		nextPosition.y = currentPosition.y;
		nextPosition.z = currentPosition.z;
	}

	public void applyGravity(final float dt) {
		state.y = currentPosition.y;

		Gravity.apply(state, dt);

		//System.out.println(jumping + " " + state);

		nextPosition.y = state.y;

	}

	private boolean checkNewPositionSingleDim(final AABB newPos) {
		final List<Location> blocksAround = Location.geLocationsAround(newPos, 1);
		Veya.scene.filterAir(blocksAround);
		return CollisionDetection.checkCollision(newPos, blocksAround);
	}

	public Vector3f getPosition() {
		return new Vector3f(this.currentPosition.x, this.currentPosition.y, this.currentPosition.z);
	}

	public Vector3f getViewDirection() {
		final float x = (float) (Math.cos(pitch) * Math.sin(yaw));
		final float y = (float) -(Math.sin(pitch));
		final float z = (float) -(Math.cos(pitch) * Math.cos(yaw));
		return new Vector3f(x, y, z);
	}

	private AABB buildAABB(final float x, final float y, final float z) {
		return new AABB(x - CAMERA_OFFSET_SIDE, y - CAMERA_OFFSET_BOTTOM, z - CAMERA_OFFSET_SIDE, x + CAMERA_OFFSET_SIDE, y + CAMERA_OFFSET_TOP, z + CAMERA_OFFSET_SIDE);
	}

	public AABB getAABB() {
		return buildAABB(currentPosition.x, currentPosition.y, currentPosition.z);
	}

	public void resetVelocity() {
		state.v = 0;
	}

}
