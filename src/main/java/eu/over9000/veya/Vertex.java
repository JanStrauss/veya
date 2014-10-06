package eu.over9000.veya;

public class Vertex {
	// Vertex data
	private float[] xyzw = new float[] { 0f, 0f, 0f, 1f };
	private float[] rgba = new float[] { 1f, 1f, 1f, 1f };
	private float[] st = new float[] { 0f, 0f };
	
	Vertex(final float x, final float y, final float z, final float r, final float g, final float b, final float s, final float t) {
		this.setXYZ(x, y, z);
		this.setRGB(r, g, b);
		this.setST(s, t);
	}
	
	// The amount of bytes an element has
	public static final int elementBytes = 4;
	
	// Elements per parameter
	public static final int positionElementCount = 4;
	public static final int colorElementCount = 4;
	public static final int textureElementCount = 2;
	
	// Bytes per parameter
	public static final int positionBytesCount = Vertex.positionElementCount * Vertex.elementBytes;
	public static final int colorByteCount = Vertex.colorElementCount * Vertex.elementBytes;
	public static final int textureByteCount = Vertex.textureElementCount * Vertex.elementBytes;
	
	// Byte offsets per parameter
	public static final int positionByteOffset = 0;
	public static final int colorByteOffset = Vertex.positionByteOffset + Vertex.positionBytesCount;
	public static final int textureByteOffset = Vertex.colorByteOffset + Vertex.colorByteCount;
	
	// The amount of elements that a vertex has
	public static final int elementCount = Vertex.positionElementCount + Vertex.colorElementCount + Vertex.textureElementCount;
	// The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
	public static final int stride = Vertex.positionBytesCount + Vertex.colorByteCount + Vertex.textureByteCount;
	
	// Setters
	public void setXYZ(final float x, final float y, final float z) {
		this.setXYZW(x, y, z, 1f);
	}
	
	public void setRGB(final float r, final float g, final float b) {
		this.setRGBA(r, g, b, 1f);
	}
	
	public void setST(final float s, final float t) {
		this.st = new float[] { s, t };
	}
	
	public void setXYZW(final float x, final float y, final float z, final float w) {
		this.xyzw = new float[] { x, y, z, w };
	}
	
	public void setRGBA(final float r, final float g, final float b, final float a) {
		this.rgba = new float[] { r, g, b, 1f };
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
		// Insert ST elements
		out[i++] = this.st[0];
		out[i++] = this.st[1];
		
		return out;
	}
	
	public float[] getXYZW() {
		return new float[] { this.xyzw[0], this.xyzw[1], this.xyzw[2], this.xyzw[3] };
	}
	
	public float[] getRGBA() {
		return new float[] { this.rgba[0], this.rgba[1], this.rgba[2], this.rgba[3] };
	}
	
	public float[] getST() {
		return new float[] { this.st[0], this.st[1] };
	}
}