package eu.over9000.veya;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Util;

public class Scene {
	private final Cube cube;
	private final List<CubeInstance> objects;
	private Light light;
	private final Program program;
	
	public Scene(final Program shader) {
		this.cube = new Cube(shader);
		
		Util.checkGLError();
		
		this.objects = new ArrayList<>();
		this.program = shader;
		
		this.light = new Light(20, 20, 20, 0.9f, 0.9f, 0.45f);
		
		// final CubeInstance instance1 = new CubeInstance(0, 0, 0);
		// final CubeInstance instance2 = new CubeInstance(0, 0, 1);
		// final CubeInstance instance3 = new CubeInstance(1, 0, 0);
		// final CubeInstance instance4 = new CubeInstance(1, 0, 1);
		//
		// final CubeInstance instance5 = new CubeInstance(0, 1, 0);
		// final CubeInstance instance6 = new CubeInstance(0, 1, 1);
		// final CubeInstance instance7 = new CubeInstance(1, 1, 0);
		// final CubeInstance instance8 = new CubeInstance(1, 1, 1);
		//
		// this.objects.add(instance1);
		// this.objects.add(instance2);
		// this.objects.add(instance3);
		// this.objects.add(instance4);
		// this.objects.add(instance5);
		// this.objects.add(instance6);
		// this.objects.add(instance7);
		// this.objects.add(instance8);
		
		final int num = 8;
		
		for (int x = -2 * num; x < 2 * num; x++) {
			for (int z = -2 * num; z < 2 * num; z++) {
				final int height = (int) (Math.sin(x / 10f) + Math.sin(z / 10f) + 3.33f) * 4;
				
				for (int i = 0; i < height; i++) {
					this.objects.add(new CubeInstance(BlockType.STONE, x, i, z));
				}
				this.objects.add(new CubeInstance(BlockType.DIRT, x, height, z));
				this.objects.add(new CubeInstance(BlockType.DIRT, x, height + 1, z));
				this.objects.add(new CubeInstance(BlockType.GRASS, x, height + 2, z));
			}
		}
		
		final CubeInstance c = new CubeInstance(BlockType.GRASS, 0, 20, 0);
		final CubeInstance d = new CubeInstance(BlockType.STONE, 2, 20, 2);
		final CubeInstance e = new CubeInstance(BlockType.DIRT, 0, 20, 2);
		final CubeInstance f = new CubeInstance(BlockType.TEST, 2, 20, 0);
		// c.rotateX(45);
		// c.rotateZ(45);
		this.objects.add(c);
		this.objects.add(d);
		this.objects.add(e);
		this.objects.add(f);
	}
	
	public void init() {
		this.light.init(this.program);
	}
	
	public void updateLight(final float x, final float y, final float z) {
		this.light = new Light(x, y, z, 0.9f, 0.9f, 0.45f);
		this.light.init(this.program);
	}
	
	public void render() {
		for (final CubeInstance cubeInstance : this.objects) {
			// update Model Matrix
			cubeInstance.updateModelMatrix(this.program);
			cubeInstance.updateTextureLookupTable(this.program);
			
			org.lwjgl.opengl.Util.checkGLError();
			
			this.cube.render();
		}
	}
	
	public void dispose() {
		this.cube.dispose();
	}
}
