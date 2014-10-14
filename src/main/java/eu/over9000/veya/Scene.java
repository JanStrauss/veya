package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;
import eu.over9000.veya.util.TextureLoader;
import eu.over9000.veya.util.WorldGen;

public class Scene {
	private final World world;
	private final Map<Chunk, ChunkVAO> chunks = new HashMap<>();
	private Light light;
	private final Program program;
	private final int texture_handle;
	
	private Matrix4f modelMatrix = new Matrix4f();
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	private static final int SEALEVEL = 64;
	
	public Scene(final Program shader) {
		this.program = shader;
		this.world = new World(1337, "Keaysea");
		this.texture_handle = TextureLoader.loadPNGTexture("BLOCKS", Scene.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);
		
		this.light = new Light(60, 200, 60, 0.9f, 0.9f, 0.45f);
		
		System.out.println("generating world..");
		
		final Random random = new Random(this.world.getSeed());
		
		final int size = 256;
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
				final List<Integer> topBlocks = new ArrayList<>();
				boolean createPre = false;
				
				for (int y = 0; y < 256; y++) {
					
					if (WorldGen.genElevation(x, y, z)) {
						this.world.setBlockAt(x, y, z, BlockType.STONE);
						createPre = true;
					} else {
						if (y <= Scene.SEALEVEL) {
							this.world.setBlockAt(x, y, z, BlockType.WATER);
						}
						if (createPre) {
							
							topBlocks.add(y - 1);
							
						}
						createPre = false;
					}
				}
				for (final Integer top : topBlocks) {
					
					this.fillTopWithDirtAndGrass(random, this.world, x, z, top);
					
				}
				
			}
			// System.out.println(x + "/" + size);
		}
		
		System.out.println("generation done, creating VAOs");
		
		for (final Chunk chunk : this.world.getLoadedChunks()) {
			this.chunks.put(chunk, new ChunkVAO(chunk, this.program));
		}
		
		System.out.println("VAOs created.");
	}
	
	private void fillTopWithDirtAndGrass(final Random random, final World world, final int x, final int z, final Integer top) {
		if (top >= Scene.SEALEVEL) {
			world.setBlockAt(x, top, z, BlockType.GRASS);
		} else {
			world.setBlockAt(x, top, z, BlockType.DIRT);
		}
		
		final int dirtHeight = 3 + random.nextInt(3);
		final int dirtLimit = top - dirtHeight;
		for (int y = top; y > dirtLimit; y--) {
			if (world.getBlockAt(x, y, z) != null) {
				if (world.getBlockAt(x, y, z).getType() == BlockType.STONE) {
					world.setBlockAt(x, y, z, BlockType.DIRT);
				}
			}
		}
	}
	
	public void init() {
		this.light.init(this.program);
		this.updateModelMatrix();
	}
	
	public void updateLight(final float x, final float y, final float z) {
		this.light = new Light(x, y, z, 0.9f, 0.9f, 0.45f);
		this.light.init(this.program);
	}
	
	public void render() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		
		this.program.enableVAttributes();
		for (final Entry<Chunk, ChunkVAO> entry : this.chunks.entrySet()) {
			if (entry.getValue() != null) {
				
				entry.getValue().render();
			}
		}
		this.program.disableVAttributes();
	}
	
	private void updateModelMatrix() {
		this.modelMatrix = new Matrix4f();
		this.modelMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.program.getUniformLocation("modelMatrix"), false, this.matrixBuffer);
	}
	
	public void dispose() {
		for (final Entry<Chunk, ChunkVAO> entry : this.chunks.entrySet()) {
			if (entry.getValue() != null) {
				entry.getValue().dispose();
				entry.getKey();
			}
		}
	}
}
