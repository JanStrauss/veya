package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import eu.over9000.veya.data.BlockType;

public class CubeInstance {
	
	private static final float calcRadConst = (float) (Math.PI / 180);
	private static final Vector3f vectorX = new Vector3f(1, 0, 0);
	private static final Vector3f vectorY = new Vector3f(0, 1, 0);
	private static final Vector3f vectorZ = new Vector3f(0, 0, 1);
	
	private final Matrix4f modelMatrix = new Matrix4f();
	private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private static final IntBuffer texLookupBuffer = BufferUtils.createIntBuffer(6 * BlockType.values().length);
	
	private final BlockType type;
	
	public CubeInstance(final BlockType type) {
		this.type = type;
	}
	
	public CubeInstance(final BlockType type, final float tx, final float ty, final float tz) {
		this.type = type;
		this.translate(tx, ty, tz);
	}
	
	public CubeInstance(final BlockType type, final float tx, final float ty, final float tz, final float sx, final float sy, final float sz) {
		this.type = type;
		this.translate(tx, ty, tz);
		this.scale(sx, sy, sz);
	}
	
	public void rotateX(final float degree) {
		Matrix4f.rotate(degree * CubeInstance.calcRadConst, CubeInstance.vectorX, this.modelMatrix, this.modelMatrix);
	}
	
	public void rotateY(final float degree) {
		Matrix4f.rotate(degree * CubeInstance.calcRadConst, CubeInstance.vectorY, this.modelMatrix, this.modelMatrix);
	}
	
	public void rotateZ(final float degree) {
		Matrix4f.rotate(degree * CubeInstance.calcRadConst, CubeInstance.vectorZ, this.modelMatrix, this.modelMatrix);
	}
	
	public void translate(final float tx, final float ty, final float tz) {
		Matrix4f.translate(new Vector3f(tx, ty, tz), this.modelMatrix, this.modelMatrix);
	}
	
	public void scale(final float sx, final float sy, final float sz) {
		Matrix4f.scale(new Vector3f(sx, sy, sz), this.modelMatrix, this.modelMatrix);
	}
	
	public Matrix4f getModelMatrix() {
		return this.modelMatrix;
	}
	
	public BlockType getBlockType() {
		return this.type;
	}
	
	public static void updateModelMatrix(final Program shader) {
		new Matrix4f().store(CubeInstance.matrixBuffer);
		CubeInstance.matrixBuffer.flip();
		GL20.glUniformMatrix4(shader.getUniformLocation("modelMatrix"), false, CubeInstance.matrixBuffer);
	}
	
	public static void updateTextureLookupTable(final Program shader) {
		CubeInstance.texLookupBuffer.put(BlockType.getTextureLookupArray());
		CubeInstance.texLookupBuffer.flip();
		GL20.glUniform1(shader.getUniformLocation("textureLookup"), CubeInstance.texLookupBuffer);
	}
	
	public Vector4f getModelPosition() {
		return new Vector4f(this.modelMatrix.m30, this.modelMatrix.m31, this.modelMatrix.m32, this.modelMatrix.m33);
	}
}