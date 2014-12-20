package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import eu.over9000.veya.data.Chunk;
import eu.over9000.veya.data.World;
import eu.over9000.veya.util.ChunkLocation;
import eu.over9000.veya.util.CoordinatesUtil;
import eu.over9000.veya.util.TextureLoader;

public class Scene {
	private final static int SCENE_CHUNKS_RANGE = 12;
	
	private final Object lock = new Object();
	private boolean camPosChanged = false;
	
	private final World world;
	private final Map<Chunk, ChunkVAO> displayed_chunks = new ConcurrentHashMap<>();
	private Light light;
	private final Program program;
	private final int texture_handle;
	private final Camera camera;
	
	private Matrix4f modelMatrix = new Matrix4f();
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	private final ConcurrentLinkedQueue<ChunkChunkVAOPair> toAdd = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<ChunkChunkVAOPair> toRemove = new ConcurrentLinkedQueue<>();
	
	private boolean alive;
	
	private int last_cam_x = 0;
	private int last_cam_y = 0;
	private int last_cam_z = 0;
	
	private final Runnable displayedChunkUpdater = new Runnable() {
		
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
			}
			
		}
	};
	
	private final Thread displayedChunkUpdaterThread;
	
	public Scene(final Program shader, final Camera camera) {
		this.alive = true;
		this.camera = camera;
		this.program = shader;
		this.world = new World(1337, "Keaysea");
		this.texture_handle = TextureLoader.loadPNGTexture("BLOCKS", Scene.class.getResourceAsStream("/textures/blocks.png"), GL13.GL_TEXTURE0);
		
		this.light = new Light(60, 250, 60, 0.9f, 0.9f, 0.45f);
		
		// System.out.println("generating world..");
		//
		// this.world.genStartChunks(8);
		//
		// System.out.println("generation done, creating VAOs");
		//
		// int i = 0;
		// final int max = this.world.getLoadedChunks().size();
		// for (final Chunk chunk : this.world.getLoadedChunks()) {
		// final ChunkVAO vao = new ChunkVAO(chunk, this.program);
		// vao.create();
		// this.displayed_chunks.put(chunk, vao);
		//
		// System.out.println(i + "/" + max);
		// Display.setTitle("VEYA | gen chunkVAO: " + i + "/" + max);
		// i++;
		// }
		//
		// System.out.println("VAOs created.");
		
		this.displayedChunkUpdaterThread = new Thread(this.displayedChunkUpdater, "DisplayedChunkUpdater");
		this.displayedChunkUpdaterThread.start();
		
	}
	
	public void init() {
		this.light.init(this.program);
		this.updateModelMatrix();
	}
	
	public void updateLight(final float x, final float y, final float z) {
		this.light = new Light(x, y, z, 0.9f, 0.9f, 0.45f);
		this.light.init(this.program);
	}
	
	private void updateDisplayedChunks() {
		
		final ChunkLocation centerChunk = new ChunkLocation(this.last_cam_x, this.last_cam_y, this.last_cam_z);
		
		final int min_x = this.last_cam_x - Scene.SCENE_CHUNKS_RANGE;
		final int max_x = this.last_cam_x + Scene.SCENE_CHUNKS_RANGE;
		final int min_y = this.last_cam_y - Scene.SCENE_CHUNKS_RANGE;
		final int max_y = this.last_cam_y + Scene.SCENE_CHUNKS_RANGE;
		final int min_z = this.last_cam_z - Scene.SCENE_CHUNKS_RANGE;
		final int max_z = this.last_cam_z + Scene.SCENE_CHUNKS_RANGE;
		
		// remove chunks outside display area
		
		for (final Entry<Chunk, ChunkVAO> entry : this.displayed_chunks.entrySet()) {
			if (!CoordinatesUtil.isBetween(entry.getKey().getChunkX(), min_x, max_x) || !CoordinatesUtil.isBetween(entry.getKey().getChunkY(), min_y, max_y)
					|| !CoordinatesUtil.isBetween(entry.getKey().getChunkZ(), min_z, max_z)) {
				this.toRemove.add(new ChunkChunkVAOPair(entry.getKey(), entry.getValue()));
			}
		}
		final List<ChunkLocation> locations = new ArrayList<>();
		// load chunks in display area
		for (int x = min_x; x <= max_x; x++) {
			for (int y = min_y; y <= max_y; y++) {
				for (int z = min_z; z <= max_z; z++) {
					locations.add(new ChunkLocation(x, y, z, centerChunk));
				}
			}
		}
		
		Collections.sort(locations);
		
		for (final ChunkLocation chunkLocation : locations) {
			final Chunk chunk = this.world.getChunkWithGenAt(chunkLocation.x, chunkLocation.y, chunkLocation.z);
			if (chunk != null) {
				
				final boolean isDisplayed = this.displayed_chunks.containsKey(chunk);
				
				if (!isDisplayed) {
					this.toAdd.add(new ChunkChunkVAOPair(chunk, new ChunkVAO(chunk, this.program)));
				}
			}
		}
		
	}
	
	public void render() {
		this.checkCameraPosition();
		
		ChunkChunkVAOPair addEntry;
		while ((addEntry = this.toAdd.poll()) != null) {
			this.displayed_chunks.put(addEntry.getChunk(), addEntry.getChunkVAO());
			addEntry.getChunkVAO().create();
		}
		
		ChunkChunkVAOPair removeEntry;
		while ((removeEntry = this.toRemove.poll()) != null) {
			this.displayed_chunks.remove(removeEntry.getChunk());
			removeEntry.getChunkVAO().dispose();
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.texture_handle);
		
		this.program.enableVAttributes();
		for (final Entry<Chunk, ChunkVAO> entry : this.displayed_chunks.entrySet()) {
			if (entry.getValue() != null) {
				
				entry.getValue().render();
			}
		}
		this.program.disableVAttributes();
	}
	
	private void checkCameraPosition() {
		final int center_x = World.worldToChunkCoordinate((int) Scene.this.camera.getPosition().getX());
		final int center_y = World.worldToChunkCoordinate((int) Scene.this.camera.getPosition().getY());
		final int center_z = World.worldToChunkCoordinate((int) Scene.this.camera.getPosition().getZ());
		
		if (center_x != this.last_cam_x || center_y != this.last_cam_y || center_z != this.last_cam_z) {
			
			this.last_cam_x = center_x;
			this.last_cam_y = center_y;
			this.last_cam_z = center_z;
			
			System.out.println("Camera changed chunk: " + center_x + "," + center_y + "," + center_z);
			
			synchronized (this.lock) {
				this.camPosChanged = true;
				this.lock.notifyAll();
			}
		}
		
	}
	
	private void updateModelMatrix() {
		this.modelMatrix = new Matrix4f();
		this.modelMatrix.store(this.matrixBuffer);
		this.matrixBuffer.flip();
		GL20.glUniformMatrix4(this.program.getUniformLocation("modelMatrix"), false, this.matrixBuffer);
	}
	
	public void dispose() {
		this.alive = false;
		this.displayedChunkUpdaterThread.interrupt();
		for (final Entry<Chunk, ChunkVAO> entry : this.displayed_chunks.entrySet()) {
			if (entry.getValue() != null) {
				entry.getValue().dispose();
			}
		}
		this.displayed_chunks.clear();
	}
	
	private class ChunkChunkVAOPair {
		private final Chunk chunk;
		private final ChunkVAO chunkVAO;
		
		public ChunkChunkVAOPair(final Chunk chunk, final ChunkVAO chunkVAO) {
			this.chunk = chunk;
			this.chunkVAO = chunkVAO;
		}
		
		public Chunk getChunk() {
			return this.chunk;
		}
		
		public ChunkVAO getChunkVAO() {
			return this.chunkVAO;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (this.chunk == null ? 0 : this.chunk.hashCode());
			result = prime * result + (this.chunkVAO == null ? 0 : this.chunkVAO.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ChunkChunkVAOPair)) {
				return false;
			}
			final ChunkChunkVAOPair other = (ChunkChunkVAOPair) obj;
			if (this.chunk == null) {
				if (other.chunk != null) {
					return false;
				}
			} else if (!this.chunk.equals(other.chunk)) {
				return false;
			}
			if (this.chunkVAO == null) {
				if (other.chunkVAO != null) {
					return false;
				}
			} else if (!this.chunkVAO.equals(other.chunkVAO)) {
				return false;
			}
			return true;
		}
		
	}
	
	public int getChunkCount() {
		return this.displayed_chunks.size();
	}
	
}
