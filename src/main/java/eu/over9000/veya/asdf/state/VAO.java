package eu.over9000.veya.asdf.state;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import eu.over9000.veya.asdf.buffer.IBO;
import eu.over9000.veya.asdf.buffer.VBO;

public class VAO {
	
	private static final class VertexAttribute {
		
		private final int size;
		private final int type;
		private final boolean normalized;
		private final int stride;
		private final int pointer;
		
		private VertexAttribute(final int size, final int type, final boolean normalized, final int stride, final int pointer) {
			
			this.size = size;
			this.type = type;
			this.normalized = normalized;
			this.stride = stride;
			this.pointer = pointer;
			
		}
		
	}
	
	private final Map<Integer, VertexAttribute> attributes = new HashMap<Integer, VertexAttribute>();
	
	private final int mode;
	private final int size;
	private final IBO ibo;
	private final VBO vbo;
	
	private int handle = -1;
	
	public VAO(final int mode, final int size, final IBO ibo, final VBO vbo) {
		
		this.mode = mode;
		this.size = size;
		this.ibo = ibo;
		this.vbo = vbo;
		
	}
	
	public final void addVertexAttribute(final int index, final int size, final int type, final boolean normalized, final int stride, final int pointer) {
		
		this.attributes.put(index, new VertexAttribute(size, type, normalized, stride, pointer));
		
	}
	
	public final void glInit() {
		
		this.handle = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(this.handle);
		
		this.vbo.glBind();
		
		for (final int i : this.attributes.keySet()) {
			
			final VertexAttribute attribute = this.attributes.get(i);
			
			GL20.glEnableVertexAttribArray(i);
			GL20.glVertexAttribPointer(i, attribute.size, attribute.type, attribute.normalized, attribute.stride, attribute.pointer);
			
		}
		
		this.ibo.glBind();
		
		GL30.glBindVertexArray(0);
		
	}
	
	public final void glDraw() {
		
		GL30.glBindVertexArray(this.handle);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		GL11.glDrawElements(this.mode, this.size, GL11.GL_UNSIGNED_INT, 0);
		
	}
	
	public final void glDispose() {
		
		GL30.glDeleteVertexArrays(this.handle);
		
	}
	
}
