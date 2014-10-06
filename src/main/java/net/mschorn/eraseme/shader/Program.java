package net.mschorn.eraseme.shader;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Program {
	
	private final List<Shader> shaders = new ArrayList<>();
	
	private int handle = -1;
	
	public Program(final Shader... shaders) {
		
		for (final Shader shader : shaders) {
			this.shaders.add(shader);
		}
		
	}
	
	public final void glInit() {
		
		this.handle = GL20.glCreateProgram();
		
		for (final Shader shader : this.shaders) {
			shader.glAttach(this.handle);
		}
		
		GL20.glLinkProgram(this.handle);
		
		if (GL11.GL_FALSE == GL20.glGetProgrami(this.handle, GL20.GL_LINK_STATUS)) {
			
			final int length = GL20.glGetProgrami(this.handle, GL20.GL_INFO_LOG_LENGTH);
			final String log = GL20.glGetProgramInfoLog(this.handle, length);
			
			System.err.println(log);
			
			GL20.glDeleteProgram(this.handle);
			
			this.handle = -1;
			
		}
		
		for (final Shader shader : this.shaders) {
			shader.glDetach(this.handle);
		}
		
	}
	
	public final void glBind() {
		
		GL20.glUseProgram(this.handle);
		
	}
	
	public final void glDispose() {
		
		if (this.handle != -1) {
			GL20.glDeleteProgram(this.handle);
		}
		
	}
	
}
