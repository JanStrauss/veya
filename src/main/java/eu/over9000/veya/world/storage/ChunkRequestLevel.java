package eu.over9000.veya.world.storage;

/**
 * Created by Jan on 28.06.2015.
 */
public enum ChunkRequestLevel {

	// CACHE => only check cache
	// DATABASE => check cache + db
	// GENERATOR -> check cache + db + generator
	// FULL => check cache + db + generator + populator

	CACHE, DATABASE, GENERATOR, POPULATED, NEIGHBORS_LOADED;

	public boolean includes(final ChunkRequestLevel other) {
		return this.ordinal() >= other.ordinal();
	}
}
