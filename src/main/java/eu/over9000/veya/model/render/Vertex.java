package eu.over9000.veya.model.render;

public class Vertex {
	// Vertex model
	private float[] xyzw = new float[] { 0f, 0f, 0f, 1f };
	private float[] rgba = new float[] { 1f, 1f, 1f, 1f };
	private float[] st = new float[] { 0f, 0f };
	private float[] n = new float[] { 0f, 0f, 0f };
	
	Vertex(final float x, final float y, final float z, final float r, final float g, final float b, final float s, final float t, final float u, final float nx, final float ny, final float nz) {
		this.setXYZ(x, y, z);
		this.setRGB(r, g, b);
		this.setSTU(s, t, u);
		this.setN(nx, ny, nz);
	}
	
	// The amount of bytes an element has
	public static final int elementBytes = 4;
	
	// Elements per parameter
	public static final int positionElementCount = 4;
	public static final int colorElementCount = 4;
	public static final int textureElementCount = 3;
	public static final int normalElementCount = 3;
	
	// Bytes per parameter
	public static final int positionBytesCount = Vertex.positionElementCount * Vertex.elementBytes;
	public static final int colorByteCount = Vertex.colorElementCount * Vertex.elementBytes;
	public static final int textureByteCount = Vertex.textureElementCount * Vertex.elementBytes;
	public static final int normalByteCount = Vertex.normalElementCount * Vertex.elementBytes;
	
	// Byte offsets per parameter
	public static final int positionByteOffset = 0;
	public static final int colorByteOffset = Vertex.positionByteOffset + Vertex.positionBytesCount;
	public static final int textureByteOffset = Vertex.colorByteOffset + Vertex.colorByteCount;
	public static final int normalByteOffset = Vertex.textureByteOffset + Vertex.textureByteCount;
	
	// The amount of elements that a vertex has
	public static final int elementCount = Vertex.positionElementCount + Vertex.colorElementCount + Vertex.textureElementCount + Vertex.normalElementCount;
	// The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
	public static final int stride = Vertex.positionBytesCount + Vertex.colorByteCount + Vertex.textureByteCount + Vertex.normalByteCount;
	
	// Setters
	public void setXYZ(final float x, final float y, final float z) {
		this.xyzw = new float[] { x, y, z, 1f };
	}
	
	public void setRGB(final float r, final float g, final float b) {
		this.rgba = new float[] { r, g, b, 1f };
	}
	
	public void setN(final float nx, final float ny, final float nz) {
		this.n = new float[] { nx, ny, nz };
	}
	
	public void setSTU(final float s, final float t, final float u) {
		this.st = new float[] { s, t, u };
	}
	
	// Getters
	public float[] getElements() {
		final float[] out = new float[Vertex.elementCount];
		int i = 0;
		
		// Insert XYZW elements
		out[i++] = this.xyzw[0];
		out[i++] = this.xyzw[1];
		out[i++] = this.xyzw[2];
		out[i++] = this.xyzw[3];
		// Insert RGBA elements
		out[i++] = this.rgba[0];
		out[i++] = this.rgba[1];
		out[i++] = this.rgba[2];
		out[i++] = this.rgba[3];
		// Insert STU elements
		out[i++] = this.st[0];
		out[i++] = this.st[1];
		out[i++] = this.st[2];
		// Insert N elements
		out[i++] = this.n[0];
		out[i++] = this.n[1];
		out[i++] = this.n[2];
		
		return out;
	}
}
