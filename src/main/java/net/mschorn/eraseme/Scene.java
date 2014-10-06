package net.mschorn.eraseme;

import java.util.ArrayList;
import java.util.List;

public class Scene {
	private final Cube cube;
	private final List<CubeInstance> objects;
	private final Shader shader;
	
	public Scene(final Shader shader) {
		this.cube = new Cube(shader);
		this.objects = new ArrayList<>();
		this.shader = shader;
		
		this.objects.add(new CubeInstance(0, 0, 0));
		this.objects.add(new CubeInstance(-1, 0, -1));
		this.objects.add(new CubeInstance(-1, 0, 0));
		this.objects.add(new CubeInstance(0, 0, -1));
	}
	
	public void render() {
		for (final CubeInstance cubeInstance : this.objects) {
			// update Model Matrix
			cubeInstance.updateModelMatrix(this.shader);
			
			this.cube.render();
		}
		
	}
}
