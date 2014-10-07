package eu.over9000.veya;

import org.lwjgl.opengl.GL20;

public class Light {
	private final float x;
	private final float y;
	private final float z;
	
	private final float r;
	private final float g;
	private final float b;
	
	Light(final float posX, final float posY, final float posZ, final float r, final float g, final float b) {
		this.x = posX;
		this.y = posY;
		this.z = posZ;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public void init(final Program program) {
		GL20.glUniform3f(program.getUniformLocation("lightPosition"), this.x, this.y, this.z);
		GL20.glUniform3f(program.getUniformLocation("lightColor"), this.r, this.g, this.b);
	}
}