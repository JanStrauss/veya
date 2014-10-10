package eu.over9000.veya;

import org.lwjgl.util.vector.Vector2f;

public class BlockFace {
	
	private static final int TEX_NUM_HORIZONTAL = 4;
	private static final int TEX_NUM_VERTICAL = 4;
	
	private static final float TEX_STEP_HORIZONTAL = 1.0f / BlockFace.TEX_NUM_HORIZONTAL;
	private static final float TEX_STEP_VERTICAL = 1.0f / BlockFace.TEX_NUM_VERTICAL;
	
	private final Vector2f texCoords00;
	private final Vector2f texCoords01;
	private final Vector2f texCoords10;
	private final Vector2f texCoords11;
	
	public static BlockFace gen(final int texIDHorizontal, final int texIDVertical) {
		return new BlockFace(texIDHorizontal, texIDVertical);
	}
	
	private BlockFace(final int texIDHorizontal, final int texIDVertical) {
		this.texCoords00 = new Vector2f(texIDHorizontal * BlockFace.TEX_STEP_HORIZONTAL, texIDVertical * BlockFace.TEX_STEP_VERTICAL);
		this.texCoords01 = new Vector2f(texIDHorizontal * BlockFace.TEX_STEP_HORIZONTAL, texIDVertical + 1 * BlockFace.TEX_STEP_VERTICAL);
		this.texCoords10 = new Vector2f(texIDHorizontal + 1 * BlockFace.TEX_STEP_HORIZONTAL, texIDVertical * BlockFace.TEX_STEP_VERTICAL);
		this.texCoords11 = new Vector2f(texIDHorizontal + 1 * BlockFace.TEX_STEP_HORIZONTAL, texIDVertical + 1 * BlockFace.TEX_STEP_VERTICAL);
	}
	
	public Vector2f getTexCoords00() {
		return this.texCoords00;
	}
	
	public Vector2f getTexCoords01() {
		return this.texCoords01;
	}
	
	public Vector2f getTexCoords10() {
		return this.texCoords10;
	}
	
	public Vector2f getTexCoords11() {
		return this.texCoords11;
	}
}