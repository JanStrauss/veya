package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
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
		
		this.light = new Light(60, 250, 60, 0.9f, 0.9f, 0.45f);
		
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
				Integer highest = 0;
				
				for (final Integer top : topBlocks) {
					
					this.fillTopWithDirtAndGrass(random, this.world, x, z, top);
					if (top > highest) {
						highest = top;
					}
				}
				
				if (highest > Scene.SEALEVEL && random.nextInt(100) > 97) {
					this.plantTree(x, highest + 1, z);
				}
			}
			System.out.println(x + "/" + size);
			Display.setTitle("VEYA | gen world: " + x + "/" + size);
		}
		
		System.out.println("generation done, creating VAOs");
		
		int i = 0;
		final int max = this.world.getLoadedChunks().size();
		for (final Chunk chunk : this.world.getLoadedChunks()) {
			this.chunks.put(chunk, new ChunkVAO(chunk, this.program));
			
			System.out.println(i + "/" + max);
			Display.setTitle("VEYA | gen chunkVAO: " + i + "/" + max);
			i++;
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
				if (world.getBlockAt(x, y, z) == BlockType.STONE) {
					world.setBlockAt(x, y, z, BlockType.DIRT);
				}
			}
		}
	}
	
	private void plantTree(final int xRoot, final int yRoot, final int zRoot) {
		for (int i = 0; i < 5; i++) {
			this.world.setBlockAtIfAir(xRoot, yRoot + i, zRoot, BlockType.WOOD);
		}
		for (int l = 0; l < 2; l++) {
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						this.world.setBlockAtIfAir(x, yRoot + 2 + l, z, BlockType.LEAVES);
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					this.world.setBlockAtIfAir(x, yRoot + 4, z, BlockType.LEAVES);
				}
			}
		}
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot - 1, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot + 1, yRoot + 5, zRoot, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot - 1, BlockType.LEAVES);
		this.world.setBlockAtIfAir(xRoot, yRoot + 5, zRoot + 1, BlockType.LEAVES);
		
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
