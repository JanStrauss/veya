package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class CubeInstance {
	
	private static final float calcRadConst = (float) (Math.PI / 180);
	private static final Vector3f vectorX = new Vector3f(1, 0, 0);
	private static final Vector3f vectorY = new Vector3f(0, 1, 0);
	private static final Vector3f vectorZ = new Vector3f(0, 0, 1);
	
	private final Matrix4f modelMatrix = new Matrix4f();
	private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private static final IntBuffer texLookupBuffer = BufferUtils.createIntBuffer(6);
	
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
	
	public void updateModelMatrix(final Program shader) {
		this.modelMatrix.store(CubeInstance.matrixBuffer);
		CubeInstance.matrixBuffer.flip();
		GL20.glUniformMatrix4(shader.getUniformLocation("modelMatrix"), false, CubeInstance.matrixBuffer);
		
	}
	
	public void updateTextureLookupTable(final Program shader) {
		CubeInstance.texLookupBuffer.put(this.type.getTextureLookupArray());
		CubeInstance.texLookupBuffer.flip();
		GL20.glUniform1(shader.getUniformLocation("textureLookup"), CubeInstance.texLookupBuffer);
	}
}