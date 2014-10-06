package eu.over9000.veya.asdf.buffer;

import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;


public class UBO {


    private final ByteBuffer buffer;
    private final int binding;

    private int handle = -1;
    private boolean dirty;


    public UBO(final int binding, final int size) {

        this.binding = binding;
        this.buffer = BufferUtils.createByteBuffer(size);

    }


    public final void glInit() {

        handle = glGenBuffers();

        glBindBuffer(GL_UNIFORM_BUFFER, handle);
        glBufferData(GL_UNIFORM_BUFFER, buffer.capacity(), GL_STREAM_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

    }


    public final void glBind() {

        if (dirty) {

            glBindBuffer(GL_UNIFORM_BUFFER, handle);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, buffer);

            dirty = false;

        }

        glBindBufferRange(GL_UNIFORM_BUFFER, binding, handle, 0, buffer.capacity());

    }


    public final void glDispose() {

        glDeleteBuffers(handle);

    }


    protected final void markDirty() {

        dirty = true;

    }


    protected final ByteBuffer getByteBuffer() {

        return buffer;

    }

}
