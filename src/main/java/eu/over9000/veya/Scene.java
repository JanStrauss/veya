package eu.over9000.veya;

import java.util.ArrayList;
import java.util.List;

public class Scene {
	private final Cube cube;
	private final List<CubeInstance> objects;
	private final Program shader;
	
	public Scene(final Program shader) {
		this.cube = new Cube(shader);
		this.objects = new ArrayList<>();
		this.shader = shader;
		
		final CubeInstance instance1 = new CubeInstance(0, 0, 0);
		final CubeInstance instance2 = new CubeInstance(0, -1, 1);
		final CubeInstance instance3 = new CubeInstance(1, 1, 0);
		final CubeInstance instance4 = new CubeInstance(1, 0, 1);
		
		this.objects.add(instance1);
		this.objects.add(instance2);
		this.objects.add(instance3);
		this.objects.add(instance4);
	}
	
	public void render() {
		for (final CubeInstance cubeInstance : this.objects) {
			// update Model Matrix
			cubeInstance.updateModelMatrix(this.shader);
			
			this.cube.render();
		}
		
	}
	
	public void dispose() {
		this.cube.dispose();
	}
}
