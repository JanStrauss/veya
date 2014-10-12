package eu.over9000.veya;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Util;

public class Scene {
	private final Cube cube;
	private final List<CubeInstance> objects;
	private Light light;
	private final Program program;
	
	public static final int CHUNK_BLOCK_COUNT = 128 * 128 * 256;
	
	public Scene(final Program shader) {
		this.cube = new Cube(shader);
		
		Util.checkGLError();
		
		this.objects = new ArrayList<>();
		this.program = shader;
		
		this.light = new Light(30, 30, 30, 0.9f, 0.9f, 0.45f);
		
		// GROUND
		
		final int num = 32;
		
		for (int x = -num; x <= num; x++) {
			for (int z = -num; z <= num; z++) {
				final int height = (int) (Math.sin(x / 10f) + Math.sin(z / 10f) + 3.33f * 1 + 5);
				
				for (int i = 0; i <= height; i++) {
					this.objects.add(new CubeInstance(BlockType.STONE, x, i, z));
				}
				this.objects.add(new CubeInstance(BlockType.DIRT, x, height, z));
				this.objects.add(new CubeInstance(BlockType.DIRT, x, height + 1, z));
				this.objects.add(new CubeInstance(BlockType.GRASS, x, height + 2, z));
			}
		}
		
		// TEST BLOCKS
		
		this.objects.add(new CubeInstance(BlockType.GRASS, 0, 20, 0));
		this.objects.add(new CubeInstance(BlockType.STONE, 2, 20, 2));
		this.objects.add(new CubeInstance(BlockType.DIRT, 0, 20, 2));
		this.objects.add(new CubeInstance(BlockType.TEST, 2, 20, 0));
		this.objects.add(new CubeInstance(BlockType.WATER, 4, 20, 0));
		this.objects.add(new CubeInstance(BlockType.WOOD, 0, 20, 4));
		this.objects.add(new CubeInstance(BlockType.LEAVES, 4, 20, 4));
		this.objects.add(new CubeInstance(BlockType.SAND, 2, 20, 4));
		this.objects.add(new CubeInstance(BlockType.IRON_ORE, 4, 20, 2));
		
		// TREE
		this.plantTree(25, 12, 25);
		
		System.out.println("SCENE HAS " + this.objects.size() + " BLOCKS");
	}
	
	private void plantTree(final int xRoot, final int yRoot, final int zRoot) {
		for (int i = 0; i < 5; i++) {
			this.objects.add(new CubeInstance(BlockType.WOOD, xRoot, yRoot + i, zRoot));
		}
		for (int l = 0; l < 2; l++) {
			for (int x = xRoot - 2; x <= xRoot + 2; x++) {
				for (int z = zRoot - 2; z <= zRoot + 2; z++) {
					if (x != xRoot || z != zRoot) {
						this.objects.add(new CubeInstance(BlockType.LEAVES, x, yRoot + 2 + l, z));
					}
				}
			}
		}
		for (int x = xRoot - 1; x <= xRoot + 1; x++) {
			for (int z = zRoot - 1; z <= zRoot + 1; z++) {
				if (x != xRoot || z != zRoot) {
					this.objects.add(new CubeInstance(BlockType.LEAVES, x, yRoot + 4, z));
				}
			}
		}
		this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot));
		this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot - 1, yRoot + 5, zRoot));
		this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot + 1, yRoot + 5, zRoot));
		this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot - 1));
		this.objects.add(new CubeInstance(BlockType.LEAVES, xRoot, yRoot + 5, zRoot + 1));
		
	}
	
	public void init() {
		this.light.init(this.program);
		this.cube.initInstanced(this.objects);
	}
	
	public void updateLight(final float x, final float y, final float z) {
		this.light = new Light(x, y, z, 0.9f, 0.9f, 0.45f);
		this.light.init(this.program);
	}
	
	public void renderInstanced() {
		this.objects.get(0);
		CubeInstance.updateTextureLookupTable(this.program);
		CubeInstance.updateModelMatrix(this.program);
		this.cube.renderInstanced(this.objects);
		
	}
	
	public void dispose() {
		this.cube.dispose();
	}
}
