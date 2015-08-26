/*
 * Veya
 * Copyright (C) 2015 s1mpl3x
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package eu.over9000.veya.rendering;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
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
	
	public static final int MAX_CHUNK_UPDATES_PER_FRAME = 1;
	private final static int SCENE_CHUNK_VIEW_RANGE = 8;
	private final static int SCENE_CHUNK_CACHE_RANGE = SCENE_CHUNK_VIEW_RANGE + 2;
	
	private final Object lock = new Object();
	private boolean camPosChanged = false;
	
	private final World world;
	
	private final Light light;
	private final Shadow shadow;

	private final int texture_handle;

	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private final Map<Chunk, ChunkVAO> displayedChunks = new ConcurrentHashMap<>();
	private final Queue<ChunkChunkVAOPair> toAdd = new ConcurrentLinkedQueue<>();

	private final Queue<ChunkChunkVAOPair> toRemove = new ConcurrentLinkedQueue<>();
	private final BlockingQueue<Chunk> toUpdateOffRender = new LinkedBlockingQueue<>();

	private final Queue<ChunkChunkVAOPair> updatedUpdateOffRender = new ConcurrentLinkedQueue<>();

	private final AtomicBoolean alive;
	public AtomicInteger chunkUpdateCounterInRender = new AtomicInteger(0);

	public AtomicInteger chunkUpdateCounterOffRender = new AtomicInteger(0);

	private final Thread displayedChunkUpdaterThread;
	private final Thread chunkVAOUpdateOffRenderThread;

	public BlockType placeBlockType = BlockType.TEST;
	private Location centerChunk = new Location(0, 0, 0);

	public Scene(final long seed) {
		this.alive = new AtomicBoolean(true);
		this.world = new World(seed, "Keaysea");
		this.texture_handle = TextureLoader.loadBlockTexture(GL13.GL_TEXTURE0);

		this.shadow = new Shadow();
		
		this.light = new Light(-200, 400, -40, 0.9f, 0.9f, 0.45f, 0.33f, 0.33f, 0.33f);
		
		final Runnable displayedChunkUpdater = () -> {
			while (Scene.this.alive.get()) {
				synchronized (Scene.this.lock) {
					try {
						while (!Scene.this.camPosChanged) {
							Scene.this.lock.wait();
						}
					} catch (final InterruptedException e) {
						if (!Scene.this.alive.get()) {
							return;
						}
						e.printStackTrace();
					}
					Scene.this.camPosChanged = false;
				}
				
				Scene.this.updateDisplayedChunks();
				world.clearCache(centerChunk, SCENE_CHUNK_CACHE_RANGE);
			}
		};
		
		final Runnable offRenderChunkUpdater = () -> {
			while (Scene.this.alive.get()) {
				try {
					final Chunk toUpdate = toUpdateOffRender.take();
					ChunkVAO chunkVAO = new ChunkVAO(toUpdate);
					updatedUpdateOffRender.add(new ChunkChunkVAOPair(toUpdate, chunkVAO));
					chunkUpdateCounterOffRender.incrementAndGet();
				} catch (InterruptedException e) {
					if (!Scene.this.alive.get()) {
						return;
					}
					e.printStackTrace();
				}
				
			}
		};
		
		
		this.displayedChunkUpdaterThread = new Thread(displayedChunkUpdater, "DisplayedChunkUpdater");
		this.displayedChunkUpdaterThread.start();
		
		this.chunkVAOUpdateOffRenderThread = new Thread(offRenderChunkUpdater, "OffRenderChunkUpdater");
		this.chunkVAOUpdateOffRenderThread.start();
		
	}
	
	public void init() {
		GL20.glUniform1i(Veya.program_normal.getUniformLocation("textureData"), 0);
		GL20.glUniform1i(Veya.program_normal.getUniformLocation("shadowMap"), 1);

		this.light.init();
		this.shadow.init();
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
				chunk.getAndResetChangedFlag();
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
		while ((addEntry = this.toAdd.poll()) != null) {
			addEntry.getChunkVAO().create();
			this.displayedChunks.put(addEntry.getChunk(), addEntry.getChunkVAO());
		}
		
		ChunkChunkVAOPair removeEntry;
		while ((removeEntry = this.toRemove.poll()) != null) {
			this.displayedChunks.remove(removeEntry.getChunk());
			removeEntry.getChunkVAO().dispose();
		}
		
		ChunkChunkVAOPair updatedEntry;
		while ((updatedEntry = this.updatedUpdateOffRender.poll()) != null) {
			updatedEntry.getChunkVAO().create();
			final ChunkVAO oldVAO = this.displayedChunks.put(updatedEntry.getChunk(), updatedEntry.getChunkVAO());
			if (oldVAO != null) {
				oldVAO.dispose();
			}
		}
		
		int updates = 0;
		for (final Chunk chunk : displayedChunks.keySet()) {
			if (world.hasChunkChanged(chunk)) {
				if (chunk.getLocation().nextTo(centerChunk)) {
					
					final ChunkVAO newVAO = new ChunkVAO(chunk);
					final ChunkVAO oldVAO = this.displayedChunks.put(chunk, newVAO);
					oldVAO.dispose();
					newVAO.create();
					chunkUpdateCounterInRender.incrementAndGet();
					updates++;
					if (updates > MAX_CHUNK_UPDATES_PER_FRAME) {
						break;
					}
				} else {
					toUpdateOffRender.add(chunk);
				}
			}
		}
		
		shadow.preRender();
		Util.checkGLError();
		internalRender(false);
		Util.checkGLError();
		shadow.postRender();
		Util.checkGLError();

		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		internalRender(true);
		
		if (Veya.debugShadow) {
			Veya.program_debug.use(true);
			GL20.glUniform1f(Veya.program_debug.getUniformLocation("near_plane"), Shadow.SHADOW_NEAR);
			GL20.glUniform1f(Veya.program_debug.getUniformLocation("far_plane"), Shadow.SHADOW_FAR);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadow.getDepthMap());


			shadow.renderQuad();
			
		}


		Veya.program_normal.use(true);
	}

	private void internalRender(final boolean transparent) {
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (entry.getValue() != null) {

				entry.getValue().render(true);
			}
		}
		//if (transparent) {
		for (final Entry<Chunk, ChunkVAO> entry : this.displayedChunks.entrySet()) {
			if (entry.getValue() != null) {

				entry.getValue().render(false);
			}
		}
		//}
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
		final Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(Veya.program_normal.getUniformLocation("modelMatrix"), false, this.matrixBuffer);

		Veya.program_shadow.use(true);
		modelMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(Veya.program_shadow.getUniformLocation("modelMatrix"), false, this.matrixBuffer);

		Veya.program_normal.use(true);
	}
	
	public void dispose() {
		this.alive.set(false);
		this.displayedChunkUpdaterThread.interrupt();
		this.chunkVAOUpdateOffRenderThread.interrupt();
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
	
	private LookAtResult getLookAtBlock() {
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
				return new LookAtResult(candidate, intersectionResult);
			}
		}
		
		return null;
	}
	
	public void performLeftClick() {
		
		final LookAtResult lookAt = getLookAtBlock();
		
		if (lookAt != null) {
			world.clearBlockAt(lookAt.location.x, lookAt.location.y, lookAt.location.z, ChunkRequestLevel.CACHE);
		}
		
	}
	
	public void performRightClick() {
		final LookAtResult lookAt = getLookAtBlock();
		
		if (lookAt != null) {
			final Location placeLocation = CollisionDetection.getNeighborBlockFromIntersectionResult(lookAt.location.x, lookAt.location.y, lookAt.location.z, lookAt.lookAtResult);
			
			final AABB blockAABB = new AABB(placeLocation);
			final AABB cameraAABB = Veya.camera.getAABB();
			
			if (!CollisionDetection.checkCollision(cameraAABB, blockAABB)) {
				world.setBlockAt(placeLocation.x, placeLocation.y, placeLocation.z, placeBlockType, ChunkRequestLevel.CACHE, true);
			}
			
		}
	}
	
	public void performMiddleClick() {
		final LookAtResult lookAt = getLookAtBlock();
		
		if (lookAt != null) {
			placeBlockType = world.getBlockAt(lookAt.location.x, lookAt.location.y, lookAt.location.z);
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
	
	public void setBlock(final BlockType type) {
		this.placeBlockType = type;
	}
	
	private class LookAtResult {
		public final int[] lookAtResult;
		public final Location location;
		
		public LookAtResult(final Location location, final int[] lookAtResult) {
			this.location = location;
			this.lookAtResult = lookAtResult;
		}
	}
}
