package eu.over9000.veya.rendering;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.over9000.veya.Veya;
import eu.over9000.veya.collision.AABB;
import eu.over9000.veya.collision.CollisionDetection;
import eu.over9000.veya.util.Location;
import eu.over9000.veya.util.MathUtil;
import eu.over9000.veya.util.TextureLoader;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;
import eu.over9000.veya.world.storage.ChunkRequestLevel;

public class Scene {

	private final static int SCENE_CHUNK_VIEW_RANGE = 8;
	private final static int SCENE_CHUNK_CACHE_RANGE = SCENE_CHUNK_VIEW_RANGE + 2;

	private final Object lock = new Object();
	private boolean camPosChanged = false;

	private final World world;

	private final Light light;
	private final int texture_handle;

	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

	private final Map<Chunk, ChunkVAO> displayedChunks = new ConcurrentHashMap<>();
	private final Queue<ChunkChunkVAOPair> toAdd = new ConcurrentLinkedQueue<>();
	private final Queue<ChunkChunkVAOPair> toRemove = new ConcurrentLinkedQueue<>();

	private boolean alive;

	private Location centerChunk = new Location(0, 0, 0);

	private final Thread displayedChunkUpdaterThread;

	public Scene(final long seed) {
		this.alive = true;
		this.world = new World(seed, "Keaysea");
		this.texture_handle = TextureLoader.loadPNGTexture("BLOCKS", Scene.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);

		this.light = new Light(0, 200, 0, 0.9f, 0.9f, 0.45f, 0.33f, 0.33f, 0.33f);

		Runnable displayedChunkUpdater = new Runnable() {

			@Override
			public void run() {
				while (Scene.this.alive) {
					synchronized (Scene.this.lock) {
						try {
							while (!Scene.this.camPosChanged) {
								Scene.this.lock.wait();
							}
						} catch (final InterruptedException e) {
							if (!Scene.this.alive) {
								return;
							}
							e.printStackTrace();
						}
						Scene.this.camPosChanged = false;
					}

					Scene.this.updateDisplayedChunks();
					world.clearCache(centerChunk, SCENE_CHUNK_CACHE_RANGE);
				}

			}
		};
		this.displayedChunkUpdaterThread = new Thread(displayedChunkUpdater, "DisplayedChunkUpdater");
		this.displayedChunkUpdaterThread.start();

	}

	public void init() {
		this.light.init();
		this.updateModelMatrix();
	}

	public Light getLight() {
		return this.light;
	}

	private boolean checkChunkInViewRange(final Chunk chunk) {

		final int min_x = centerChunk.x - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_x = centerChunk.x + Scene.SCENE_CHUNK_VIEW_RANGE;
		final int min_y = centerChunk.y - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_y = centerChunk.y + Scene.SCENE_CHUNK_VIEW_RANGE;
		final int min_z = centerChunk.z - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_z = centerChunk.z + Scene.SCENE_CHUNK_VIEW_RANGE;

		return MathUtil.isBetween(chunk.getChunkX(), min_x, max_x) && MathUtil.isBetween(chunk.getChunkY(), min_y, max_y) && MathUtil.isBetween(chunk.getChunkZ(), min_z, max_z);
	}

	private void updateDisplayedChunks() {
		final int min_x = centerChunk.x - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_x = centerChunk.x + Scene.SCENE_CHUNK_VIEW_RANGE;
		final int min_y = centerChunk.y - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_y = centerChunk.y + Scene.SCENE_CHUNK_VIEW_RANGE;
		final int min_z = centerChunk.z - Scene.SCENE_CHUNK_VIEW_RANGE;
		final int max_z = centerChunk.z + Scene.SCENE_CHUNK_VIEW_RANGE;

		// remove chunks outside display area
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (!checkChunkInViewRange(entry.getKey())) {

				final ChunkChunkVAOPair candidate = new ChunkChunkVAOPair(entry.getKey(), entry.getValue());
				if (!toRemove.contains(candidate)) {
					this.toRemove.add(candidate);
				}
			}
		}

		final List<Location> locations = new ArrayList<>();
		// load chunks in display area
		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				for (int z = min_z; z <= max_z; z++) {
					locations.add(new Location(x, y, z, centerChunk));
				}
			}
		}

		Collections.sort(locations);

		for (final Location chunkLocation : locations) {
			final Chunk chunk = this.world.getChunkAt(chunkLocation.x, chunkLocation.y, chunkLocation.z, ChunkRequestLevel.NEIGHBORS_LOADED, false);

			if (chunk == null) {
				continue;
			}

			final boolean isDisplayed = this.displayedChunks.containsKey(chunk);

			if (!isDisplayed) {
				final ChunkChunkVAOPair candidate = new ChunkChunkVAOPair(chunk, new ChunkVAO(chunk));
				if (!toAdd.contains(candidate)) {
					this.toAdd.add(candidate);
				}
			}
		}
	}

	public void render() {
		this.checkCameraPosition();

		ChunkChunkVAOPair addEntry;
		if ((addEntry = this.toAdd.poll()) != null) {
			this.displayedChunks.put(addEntry.getChunk(), addEntry.getChunkVAO());
			addEntry.getChunkVAO().create();
		}

		ChunkChunkVAOPair removeEntry;
		if ((removeEntry = this.toRemove.poll()) != null) {
			this.displayedChunks.remove(removeEntry.getChunk());
			removeEntry.getChunkVAO().dispose();
		}

		for (final Chunk chunk : displayedChunks.keySet()) {
			if (world.hasChunkChanged(chunk)) {
				final ChunkVAO oldVAO = this.displayedChunks.get(chunk);
				oldVAO.dispose();
				final ChunkVAO newVAO = new ChunkVAO(chunk);
				this.displayedChunks.put(chunk, newVAO);
				newVAO.create();
			}
		}

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);

		Veya.program.enableVAttributes();
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (entry.getValue() != null) {

				entry.getValue().render(true);
			}
		}
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (entry.getValue() != null) {

				entry.getValue().render(false);
			}
		}
		Veya.program.disableVAttributes();
	}

	private void checkCameraPosition() {
		final int center_x = World.worldToChunkCoordinate((int) Veya.camera.getPosition().getX());
		final int center_y = World.worldToChunkCoordinate((int) Veya.camera.getPosition().getY());
		final int center_z = World.worldToChunkCoordinate((int) Veya.camera.getPosition().getZ());

		if (center_x != centerChunk.x || center_y != centerChunk.y || center_z != centerChunk.z) {

			centerChunk = new Location(center_x, center_y, center_z);

			System.out.println("Camera changed chunk: " + center_x + "," + center_y + "," + center_z);

			synchronized (this.lock) {
				this.camPosChanged = true;
				this.lock.notifyAll();
			}
		}

	}

	private void updateModelMatrix() {
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(Veya.program.getUniformLocation("modelMatrix"), false, this.matrixBuffer);
	}

	public void dispose() {
		this.alive = false;
		this.displayedChunkUpdaterThread.interrupt();
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (entry.getValue() != null) {
				entry.getValue().dispose();
			}
		}
		this.world.onExit();
		this.displayedChunks.clear();
	}

	public World getWorld() {
		return world;
	}

	public void performLeftClick() {
		final Vector3f position = Veya.camera.getPosition();
		final Vector3f viewDirection = Veya.camera.getViewDirection();

		final List<Location> candidates = Location.getLocationsAround((int) position.x, (int) position.y, (int) position.z, 3);
		Collections.sort(candidates);

		for (final Location candidate : candidates) {
			final BlockType type = world.getBlockAt(candidate.x, candidate.y, candidate.z);

			if (type == null || type == BlockType.BEDROCK) {
				continue;
			}

			final int[] intersectionResult = CollisionDetection.checkCollision(position, viewDirection, candidate.x, candidate.y, candidate.z);

			if (intersectionResult != null) {
				//System.out.println("found collision with block at " + candidate.y + " " + candidate.y + " " + candidate.z + " with type " + type);
				world.clearBlockAt(candidate.x, candidate.y, candidate.z, ChunkRequestLevel.CACHE);
				break;
			}
		}
	}

	public void performRightClick() {
		final Vector3f position = Veya.camera.getPosition();
		final Vector3f viewDirection = Veya.camera.getViewDirection();

		final List<Location> candidates = Location.getLocationsAround((int) position.x, (int) position.y, (int) position.z, 4);
		Collections.sort(candidates);

		for (final Location candidate : candidates) {
			final BlockType type = world.getBlockAt(candidate.x, candidate.y, candidate.z);

			if (type == null) {
				continue;
			}

			final int[] intersectionResult = CollisionDetection.checkCollision(position, viewDirection, candidate.x, candidate.y, candidate.z);

			if (intersectionResult != null) {
				//System.out.println("found collision with block at " + candidate.y + " " + candidate.y + " " + candidate.z + " with type " + type);

				final Location placeLocation = CollisionDetection.getNeighborBlockFromIntersectionResult(candidate.x, candidate.y, candidate.z, intersectionResult);

				final AABB blockAABB = new AABB(placeLocation);
				final AABB cameraAABB = Veya.camera.getAABB();

				if (!CollisionDetection.checkCollision(cameraAABB, blockAABB)) {
					world.setBlockAt(placeLocation.x, placeLocation.y, placeLocation.z, BlockType.TEST, ChunkRequestLevel.CACHE, true);
				}

				break;
			}
		}
	}

	public void onNewChunk(final Chunk chunk) {
		if (checkChunkInViewRange(chunk)) {
			toAdd.add(new ChunkChunkVAOPair(chunk, new ChunkVAO(chunk)));

		}
	}

	public int getChunkCount() {
		return this.displayedChunks.size();
	}

	public void filterAir(final List<Location> locations) {
		for (final Iterator<Location> iterator = locations.iterator(); iterator.hasNext(); ) {
			final Location location = iterator.next();
			if (world.getBlockAt(location) == null || Veya.ignoreBlocks.contains(world.getBlockAt(location))) {
				iterator.remove();
			}
		}
	}

}
