package net.mschorn.eraseme.state;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import net.mschorn.eraseme.buffer.IBO;
import net.mschorn.eraseme.buffer.VBO;


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

        attributes.put(index, new VertexAttribute(size, type, normalized, stride, pointer));

    }


    public final void glInit() {

        handle = glGenVertexArrays();

        glBindVertexArray(handle);

        vbo.glBind();

        for (final int i : attributes.keySet()) {

            final VertexAttribute attribute = attributes.get(i);

            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, attribute.size, attribute.type, attribute.normalized, attribute.stride, attribute.pointer);

        }

        ibo.glBind();

        glBindVertexArray(0);

    }


    public final void glDraw() {

        glBindVertexArray(handle);
        glDrawElements(mode, size, GL_UNSIGNED_INT, 0);

    }


    public final void glDispose() {

        glDeleteVertexArrays(handle);

    }

}
