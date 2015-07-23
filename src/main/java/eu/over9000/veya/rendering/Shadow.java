package eu.over9000.veya.rendering;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.Veya;
import eu.over9000.veya.util.MatrixUtil;

/**
 * Created by Jan on 22.07.2015.
 */
public class Shadow {
	public static final int SHADOW_WIDTH = 1024;
	public static final int SHADOW_HEIGHT = 1024;

	public static final float SHADOW_RANGE = 275.0f;
	public static final float SHADOW_NEAR = 1f;
	public static final float SHADOW_FAR = 750.0f;

	private int shadowFBO;
	private int depthMap;

	public void init() {
		depthMap = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthMap);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);

		shadowFBO = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthMap, 0);
		GL11.glDrawBuffer(GL11.GL_NONE);
		GL11.glReadBuffer(GL11.GL_NONE);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	private void updateMatrixLightSpaceMatrixLocation() {
		final FloatBuffer matrixBufferShadow = BufferUtils.createFloatBuffer(16);
		final FloatBuffer matrixBufferNormal = BufferUtils.createFloatBuffer(16);

		final Matrix4f lightProjection = MatrixUtil.orthographic(-SHADOW_RANGE, +SHADOW_RANGE, -SHADOW_RANGE, +SHADOW_RANGE, SHADOW_NEAR, SHADOW_FAR);
		//final Matrix4f lightProjection = MatrixUtil.projection(75, SHADOW_WIDTH, SHADOW_HEIGHT, SHADOW_NEAR, SHADOW_FAR);
		final Matrix4f lightView = MatrixUtil.lookAtCenter(Veya.scene.getLight().getPosition(), new Vector3f(0, 0, 0));
		final Matrix4f lightSpaceMatrix = Matrix4f.mul(lightProjection, lightView, null);

		lightSpaceMatrix.store(matrixBufferShadow);
		lightSpaceMatrix.store(matrixBufferNormal);

		matrixBufferShadow.flip();
		matrixBufferNormal.flip();

		Veya.program_shadow.use(true);
		GL20.glUniformMatrix4(Veya.program_shadow.getUniformLocation("lightSpaceMatrix"), false, matrixBufferShadow);

		Veya.program_normal.use(true);
		GL20.glUniformMatrix4(Veya.program_normal.getUniformLocation("lightSpaceMatrix"), false, matrixBufferNormal);

		Veya.program_shadow.use(true);
	}

	public void preRender() {
		Veya.program_shadow.use(true);
		updateMatrixLightSpaceMatrixLocation();
		GL11.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFBO);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glCullFace(GL11.GL_FRONT);

	}

	public void postRender() {
		GL11.glCullFace(GL11.GL_BACK); // don't forget to reset original culling face
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		Veya.program_normal.use(true);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());

		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthMap);
	}

	int quadVAO = 0;
	int quadVBO;

	public void renderQuad() {
		if (quadVAO == 0) {
			final float[] quadVertices = {
					// Positions        // Texture Coords
					-1.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f,};
			// Setup plane VAO

			final FloatBuffer fb = BufferUtils.createFloatBuffer(quadVertices.length);

			fb.put(quadVertices);
			fb.flip();

			quadVAO = GL30.glGenVertexArrays();
			quadVBO = GL15.glGenBuffers();
			GL30.glBindVertexArray(quadVAO);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		}
		GL30.glBindVertexArray(quadVAO);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		GL30.glBindVertexArray(0);
	}

	public int getDepthMap() {
		return depthMap;
	}
}
