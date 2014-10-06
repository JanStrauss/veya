package eu.over9000.veya.asdf.shader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.ArrayList;
import java.util.List;


public class Program {


    private final List<Shader> shaders = new ArrayList<>();

    public int handle = -1;


    public Program(final Shader... shaders) {

        for (final Shader shader : shaders)
            this.shaders.add(shader);

    }


    public final void glInit() {

        handle = glCreateProgram();

        for (final Shader shader : shaders)
            shader.glAttach(handle);

        glLinkProgram(handle);

        if (GL_FALSE == glGetProgrami(handle, GL_LINK_STATUS)) {

            final int length = glGetProgrami(handle, GL_INFO_LOG_LENGTH);
            final String log = glGetProgramInfoLog(handle, length);

            System.err.println(log);

            glDeleteProgram(handle);

            handle = -1;

        }

        for (final Shader shader : shaders)
            shader.glDetach(handle);

    }


    public final void glBind() {

        glUseProgram(handle);

    }


    public final void glDispose() {

        if (handle != -1)
            glDeleteProgram(handle);

    }

}
