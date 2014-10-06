package eu.over9000.veya.asdf.buffer;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;


public class IBO {


    private final IntBuffer buffer;
    private final int usage;

    private int handle = -1;


    public IBO(final int[] values, final int usage) {

        this.buffer = BufferUtils.createIntBuffer(values.length);
        this.usage = usage;

        buffer.put(values);
        buffer.flip();

    }


    public final void glInit() {

        handle = glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, usage);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    }


    public final void glBind() {

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, handle);

    }


    public final void glDispose() {

        glDeleteBuffers(handle);

    }

}
