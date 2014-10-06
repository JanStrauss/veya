package eu.over9000.veya.asdf.buffer;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;


public class VBO {


    private final FloatBuffer buffer;
    private final int usage;

    private int handle = -1;


    public VBO(final float[] values, final int usage) {

        this.buffer = BufferUtils.createFloatBuffer(values.length);
        this.usage = usage;

        buffer.put(values);
        buffer.flip();

    }


    public final void glInit() {

        handle = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, handle);
        glBufferData(GL_ARRAY_BUFFER, buffer, usage);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }


    public final void glBind() {

        glBindBuffer(GL_ARRAY_BUFFER, handle);

    }


    public final void glDispose() {

        glDeleteBuffers(handle);

    }

}
