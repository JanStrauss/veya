package eu.over9000.veya.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextureLoader {
	
	private static final int TEXTURE_WIDTH = 64;
	private static final int TEXTURE_HEIGHT = 64;

	public static int loadFontTexture(final int textureUnit) {

		final int texId = GL11.glGenTextures();


		try {
			final InputStream in = TextureLoader.class.getResourceAsStream("/textures/font.png");
			final PNGDecoder decoder = new PNGDecoder(in);
			
			final int sourceTexWidth = decoder.getWidth();
			final int sourceTexHeight = decoder.getHeight();

			final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(byteBuffer, decoder.getWidth() * 4, Format.RGBA);
			byteBuffer.flip();

			GL13.glActiveTexture(textureUnit);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, sourceTexWidth, sourceTexHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);

			// Setup the ST coordinate system
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

			// Setup what to do when the texture has to be scaled
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

			org.lwjgl.opengl.Util.checkGLError();

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		

		return texId;
	}
	
	public static int loadBlockTexture(final int textureUnit) {

		final InputStream in = TextureLoader.class.getResourceAsStream("/textures/blocks.png");
		ByteBuffer convertedBuffer = null;
		int sourceTexWidth = 0;
		int sourceTexHeight = 0;
		int texCount = 0;
		
		try {
			// Link the PNG decoder to this stream
			final PNGDecoder decoder = new PNGDecoder(in);
			
			// Get the width and height of the texture
			sourceTexWidth = decoder.getWidth();
			sourceTexHeight = decoder.getHeight();
			
			texCount = sourceTexWidth / TextureLoader.TEXTURE_WIDTH * (sourceTexHeight / TextureLoader.TEXTURE_WIDTH);
			
			// Decode the PNG file in a ByteBuffer
			final ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			
			convertedBuffer = TextureLoader.convertBuffer(buf, sourceTexWidth, sourceTexHeight);
			
			in.close();
			
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Create a new texture object in memory and bind it
		final int texId = GL11.glGenTextures();
		GL13.glActiveTexture(textureUnit);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, texId);
		
		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		
		// Upload the texture model and generate mip maps (for scaling)
		// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL11.GL_RGBA, TextureLoader.TEXTURE_WIDTH, TextureLoader.TEXTURE_HEIGHT, texCount, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, convertedBuffer);
		GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
		
		// Setup the ST coordinate system
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
		
		org.lwjgl.opengl.Util.checkGLError();
		
		System.out.println("loading block texture, id=" + texId + ", w=" + sourceTexWidth + ", h=" + sourceTexHeight);

		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		
		return texId;
	}
	
	private static ByteBuffer convertBuffer(final ByteBuffer orig, final int sourceWidth, final int sourceHeight) {
		final ByteBuffer result = ByteBuffer.allocateDirect(sourceWidth * sourceHeight * 4);
		
		final int cellWidth = sourceWidth / TextureLoader.TEXTURE_WIDTH;
		final int cellHeight = sourceHeight / TextureLoader.TEXTURE_HEIGHT;
		
		int i = 0;
		for (int y_cell = 0; y_cell < cellHeight; y_cell++) {
			for (int x_cell = 0; x_cell < cellWidth; x_cell++) {
				for (int y = 0; y < TextureLoader.TEXTURE_HEIGHT; y++) {
					for (int x = 0; x < TextureLoader.TEXTURE_WIDTH; x++) {
						final int coordOrig = 4 * ((y + y_cell * TextureLoader.TEXTURE_HEIGHT) * sourceWidth + x + x_cell * TextureLoader.TEXTURE_WIDTH);
						
						final byte r = orig.get(coordOrig + 0);
						final byte g = orig.get(coordOrig + 1);
						final byte b = orig.get(coordOrig + 2);
						final byte a = orig.get(coordOrig + 3);
						
						result.put(r);
						result.put(g);
						result.put(b);
						result.put(a);
						
						// System.out.println("i=" + i + " cO=" + coordOrig + " | r=" + r + " g=" + g + " b= " + b + " a=" + a);
						i++;
					}
				}
			}
		}
		result.flip();
		return result;
	}
	
}
