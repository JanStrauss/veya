package veya;

public class Cube {
	
	private static final int RESTART = 0xFFFFFFFF;
	
	//@formatter:off
	 private static final int[] indexBuffer = new int[] {
		 
		 0, 1, 2, 3, Cube.RESTART,
		 5, 1, 4, 0, Cube.RESTART,
		 0, 2, 4, 6, Cube.RESTART,
		 4, 6, 5, 7, Cube.RESTART,
		 1, 5, 3, 7, Cube.RESTART,
		 2, 3, 6, 7
	};
		  
	// x, y, z, r, g, b
	private static final float[] vertexBuffer = new float[] {
		 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
		 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
		 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
		 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
		 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
		 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
		 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
		 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
		 };
	
	//@formatter:on
}