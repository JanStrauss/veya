package net.mschorn.eraseme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.OpenGLException;

public class Shader {
	public final int id;
	public final Map<String, Integer> uniformLocations;
	public final Map<String, Integer> attribLocations;
	
	public Shader(final String[] attribNames, final String[] uniformNames) {
		final int vsID = Shader.loadShader(new File(Shader.class.getResource("vs_new.glsl").getFile()), GL20.GL_VERTEX_SHADER);
		final int fsID = Shader.loadShader(new File(Shader.class.getResource("fs_new.glsl").getFile()), GL20.GL_FRAGMENT_SHADER);
		
		this.id = GL20.glCreateProgram();
		GL20.glAttachShader(this.id, vsID);
		GL20.glAttachShader(this.id, fsID);
		
		GL20.glLinkProgram(this.id);
		Shader.checkLinkage(this.id);
		
		GL20.glValidateProgram(this.id);
		Shader.checkValidation(this.id);
		
		GL20.glDetachShader(this.id, vsID);
		GL20.glDetachShader(this.id, fsID);
		
		GL20.glDeleteShader(vsID);
		GL20.glDeleteShader(fsID);
		
		this.attribLocations = new HashMap<>(attribNames.length + 1, 1);
		for (final String name : attribNames) {
			this.attribLocations.put(name, GL20.glGetAttribLocation(this.id, name));
		}
		
		this.uniformLocations = new HashMap<>(uniformNames.length + 1, 1);
		for (final String name : uniformNames) {
			this.uniformLocations.put(name, GL20.glGetUniformLocation(this.id, name));
		}
	}
	
	public void use(final boolean use) {
		if (use) {
			GL20.glUseProgram(this.id);
		} else {
			GL20.glUseProgram(0);
		}
	}
	
	public void unload() {
		GL20.glDeleteProgram(this.id);
	}
	
	public int getAttribLocation(final String name) {
		return this.attribLocations.get(name);
	}
	
	public int getUniformLocation(final String name) {
		return this.uniformLocations.get(name);
	}
	
	public void enableVAttributes() {
		for (final String s : this.attribLocations.keySet()) {
			GL20.glEnableVertexAttribArray(this.attribLocations.get(s));
		}
	}
	
	public void disableVAttributes() {
		for (final String s : this.attribLocations.keySet()) {
			GL20.glDisableVertexAttribArray(this.attribLocations.get(s));
		}
	}
	
	private static int loadShader(final File file, final int type) {
		final StringBuilder builder = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\n");
			}
			reader.close();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		
		final int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, builder);
		GL20.glCompileShader(shaderID);
		
		Shader.checkCompilation(shaderID);
		
		return shaderID;
	}
	
	private static void checkValidation(final int programID) {
		final int error = GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS);
		if (error == GL11.GL_FALSE) {
			throw new OpenGLException(GL20.glGetProgramInfoLog(programID, GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH)));
		} else {
			System.out.println("Shader Validation OK");
		}
	}
	
	private static void checkCompilation(final int shaderID) {
		final int error = GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS);
		if (error == GL11.GL_FALSE) {
			throw new OpenGLException(GL20.glGetShaderInfoLog(shaderID, GL20.glGetShaderi(shaderID, GL20.GL_INFO_LOG_LENGTH)));
		} else {
			System.out.println("Shader Compilation OK");
		}
	}
	
	private static void checkLinkage(final int programID) {
		final int error = GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS);
		if (error == GL11.GL_FALSE) {
			throw new OpenGLException(GL20.glGetProgramInfoLog(programID, GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH)));
		} else {
			System.out.println("Shader Linkage OK");
		}
	}
}