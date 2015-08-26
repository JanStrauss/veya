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

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.Veya;

public class Light {
	private int lightPositionLocation;
	private int lightColorLocation;
	private int lightFactorsLocation;
	
	private float x;
	private float y;
	private float z;
	
	private float r;
	private float g;
	private float b;

	private float ambient;
	private float diffuse;
	private float specular;

	public Light(final float posX, final float posY, final float posZ, final float r, final float g, final float b, final float ambient, final float diffuse, final float specular) {
		this.x = posX;
		this.y = posY;
		this.z = posZ;
		this.r = r;
		this.g = g;
		this.b = b;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}
	
	public void init() {
		this.lightPositionLocation = Veya.program_normal.getUniformLocation("lightPosition");
		this.lightColorLocation = Veya.program_normal.getUniformLocation("lightColor");
		this.lightFactorsLocation = Veya.program_normal.getUniformLocation("lightFactors");
		
		GL20.glUniform3f(this.lightPositionLocation, this.x, this.y, this.z);
		GL20.glUniform3f(this.lightColorLocation, this.r, this.g, this.b);
		GL20.glUniform3f(this.lightFactorsLocation, this.ambient, this.diffuse, this.specular);
	}
	
	public void updateLightFactors(final float ambient, final float diffuse, final float specular) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;

		GL20.glUniform3f(this.lightFactorsLocation, this.ambient, this.diffuse, this.specular);
	}
	
	public void updateLightPosition(final float posX, final float posY, final float posZ) {
		this.x = posX;
		this.y = posY;
		this.z = posZ;
		
		GL20.glUniform3f(this.lightPositionLocation, this.x, this.y, this.z);
	}
	
	public void updateLightColor(final float r, final float g, final float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		
		GL20.glUniform3f(this.lightColorLocation, this.r, this.g, this.b);
	}
	
	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}
	
	public Vector3f getPosition() {
		return new Vector3f(x, y, z);
	}
}