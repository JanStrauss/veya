package eu.over9000.veya.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Program {
	private final int id;
	private final Map<String, Integer> uniformLocations;
	private final Map<String, Integer> attribLocations;
	
	public Program(final String[] attribNames, final String[] uniformNames) {
		final int vsID = Program.loadShader(Program.class.getResourceAsStream("/shaders/vertex.glsl"), GL20.GL_VERTEX_SHADER);
		final int fsID = Program.loadShader(Program.class.getResourceAsStream("/shaders/fragment.glsl"), GL20.GL_FRAGMENT_SHADER);
		
		this.id = GL20.glCreateProgram();
		GL20.glAttachShader(this.id, vsID);
		GL20.glAttachShader(this.id, fsID);
		
		GL20.glLinkProgram(this.id);
		Program.checkLinkage(this.id);
		
		GL20.glValidateProgram(this.id);
		Program.checkValidation(this.id);
		
		GL20.glDetachShader(this.id, vsID);
		GL20.glDetachShader(this.id, fsID);
		
		GL20.glDeleteShader(vsID);
		GL20.glDeleteShader(fsID);
		
		this.attribLocations = new HashMap<>(attribNames.length + 1, 1);
		for (final String name : attribNames) {
			final int loc = GL20.glGetAttribLocation(this.id, name);
			this.attribLocations.put(name, loc);
			System.out.println("AttributeLoc for " + name + "=" + loc);
		}
		
		this.uniformLocations = new HashMap<>(uniformNames.length + 1, 1);
		for (final String name : uniformNames) {
			final int loc = GL20.glGetUniformLocation(this.id, name);
			this.uniformLocations.put(name, loc);
			System.out.println("UniformLoc for " + name + "=" + loc);
		}
		
		Util.checkGLError();
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
	
	private static int loadShader(final InputStream inputStream, final int type) {
		final StringBuilder builder = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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
		
		Program.checkCompilation(shaderID);
		
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