package net.mschorn.eraseme.shader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


public class Shader {


    private static final Charset CHARSET = Charset.forName("US-ASCII");

    private final int type;
    private final String source;

    private int handle = -1;


    public Shader(final int type, final InputStream is) {

        this.type = type;
        this.source = read(is);

    }


    private String read(final InputStream is) {

        String s;
        final StringBuilder sb = new StringBuilder();
        try (final BufferedReader rb = new BufferedReader(new InputStreamReader(is, CHARSET))) {

            while ((s = rb.readLine()) != null)
                sb.append(s).append('\n');

            return sb.toString();

        } catch (final IOException e) {

            throw new RuntimeException(e);

        }

    }


    public final void glInit() {

        handle = glCreateShader(type);

        glShaderSource(handle, source);
        glCompileShader(handle);

        if (GL_FALSE == glGetShaderi(handle, GL_COMPILE_STATUS)) {

            final int length = glGetShaderi(handle, GL_INFO_LOG_LENGTH);
            final String log = glGetShaderInfoLog(handle, length);

            System.err.println(log);

            glDeleteShader(handle);

            handle = -1;

        }

    }


    public final void glAttach(final int program) {

        glAttachShader(program, handle);

    }


    public final void glDetach(final int program) {

        glDetachShader(program, handle);

    }


    public final void glDispose() {

        if (handle != -1)
            glDeleteShader(handle);

    }

}
