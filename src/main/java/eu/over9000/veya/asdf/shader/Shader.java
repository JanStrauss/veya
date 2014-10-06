package eu.over9000.veya.asdf.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	
	private static final Charset CHARSET = Charset.forName("UTF-8");
	
	private final int type;
	private final String source;
	
	private int handle = -1;
	
	public Shader(final int type, final InputStream is) {
		
		this.type = type;
		this.source = this.read(is);
		
	}
	
	private String read(final InputStream is) {
		
		String s;
		final StringBuilder sb = new StringBuilder();
		try (final BufferedReader rb = new BufferedReader(new InputStreamReader(is, Shader.CHARSET))) {
			
			while ((s = rb.readLine()) != null) {
				sb.append(s).append('\n');
			}
			
			return sb.toString();
			
		} catch (final IOException e) {
			
			throw new RuntimeException(e);
			
		}
		
	}
	
	public final void glInit() {
		
		this.handle = GL20.glCreateShader(this.type);
		
		GL20.glShaderSource(this.handle, this.source);
		GL20.glCompileShader(this.handle);
		
		if (GL11.GL_FALSE == GL20.glGetShaderi(this.handle, GL20.GL_COMPILE_STATUS)) {
			
			final int length = GL20.glGetShaderi(this.handle, GL20.GL_INFO_LOG_LENGTH);
			final String log = GL20.glGetShaderInfoLog(this.handle, length);
			
			System.err.println(log);
			
			GL20.glDeleteShader(this.handle);
			
			this.handle = -1;
			
		}
		
	}
	
	public final void glAttach(final int program) {
		
		GL20.glAttachShader(program, this.handle);
		
	}
	
	public final void glDetach(final int program) {
		
		GL20.glDetachShader(program, this.handle);
		
	}
	
	public final void glDispose() {
		
		if (this.handle != -1) {
			GL20.glDeleteShader(this.handle);
		}
		
	}
	
}
