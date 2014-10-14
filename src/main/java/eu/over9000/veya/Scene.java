package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.data.BlockType;
import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;

public class Scene {
	private final World world;
	private final Map<Chunk, ChunkVAO> chunks = new HashMap<>();
	private Light light;
	private final Program program;
	private final int texture_handle;
	
	private Matrix4f modelMatrix = new Matrix4f();
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	public static final int CHUNK_BLOCK_COUNT = 128 * 128 * 256;
	
	public Scene(final Program shader) {
		this.program = shader;
		this.world = new World(1337, "Keaysea");
		this.texture_handle = TextureUtil.loadPNGTexture("BLOCKS", Cube.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);
		
		this.light = new Light(30, 30, 30, 0.9f, 0.9f, 0.45f);
		
		final int size = 256;
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
				
				final int height = (int) ((Math.sin(x / 15f) + Math.sin(z / 15f)) * 10f + 20);
				for (int y = 0; y <= height; y++) {
					this.world.setBlockAt(x, y, z, BlockType.STONE);
				}
				this.world.setBlockAt(x, height + 1, z, BlockType.DIRT);
				this.world.setBlockAt(x, height + 2, z, BlockType.DIRT);
				this.world.setBlockAt(x, height + 3, z, BlockType.GRASS);
			}
			System.out.println(x + "/" + size);
		}
		
		for (final Chunk chunk : this.world.getLoadedChunks()) {
			this.chunks.put(chunk, new ChunkVAO(chunk, this.program));
		}
		
	}
	
	// private void plantTree(final int xRoot, final int yRoot, final int zRoot) {
	// for (int i = 0; i < 5; i++) {
	// this.objects.add(new CubeInstance(BlockType.WOOD, xRoot, yRoot + i, zRoot));
	// }
	// for (int l = 0; l < 2; l++) {
	// for (int x = xRoot - 2; x <= xRoot + 2; x++) {
	// for (int z = zRoot - 2; z <= zRoot + 2; z++) {
	// if (x != xRoot || z != zRoot) {
	// this.objects.add(new CubeInstance(BlockType.LEAVES, x, yRoot + 2 + l, z));
	// }
	// }
	// }
	// }
	// for (int x = xRoot - 1; x <= xRoot + 1; x++) {
	// for (int z = zRoot - 1; z <= zRoot + 1; z++) {
	// if (x != xRoot || z != zRoot) {
	// this.objects.add(new CubeInstance(BlockType.LEAVES, x, yRoot + 4, z));
	// }
	// }
	// }
	// this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot));
	// this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot - 1, yRoot + 5, zRoot));
	// this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot + 1, yRoot + 5, zRoot));
	// this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot - 1));
	// this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot + 1));
	//
	// }
	
	public void init() {
		this.light.init(this.program);
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
				this.updateModelMatrix(entry.getKey());
				entry.getValue().render();
			}
		}
		this.program.disableVAttributes();
	}
	
	private void updateModelMatrix(final Chunk chunk) {
		this.modelMatrix = new Matrix4f();
		this.modelMatrix.translate(new Vector3f(chunk.getChunkX() * Chunk.CHUNK_SIZE, chunk.getChunkY() * Chunk.CHUNK_SIZE, chunk.getChunkZ() * Chunk.CHUNK_SIZE));
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
