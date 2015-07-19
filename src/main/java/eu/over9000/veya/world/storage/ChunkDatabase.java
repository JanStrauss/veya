package eu.over9000.veya.world.storage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import eu.over9000.veya.proto.ProtoDefinitions;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;

/**
 * Created by Jan on 28.06.2015.
 */
public class ChunkDatabase {

	private Connection connection;
	private boolean started = false;

	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS `chunk_stacks` (`world` VARCHAR(255), `chunkX` INTEGER , `chunkZ` INTEGER ,`populated` BOOLEAN ,`data` LONGVARBINARY  , PRIMARY KEY (`world`,`chunkX`,`chunkZ`))";
	private static final String STORE_STATEMENT = "MERGE INTO `chunk_stacks` (`world`, `chunkX`, `chunkZ`, `populated`, `data`) VALUES (?,?,?,?,?)";
	private static final String LOAD_STATEMENT = "SELECT * FROM `chunk_stacks` WHERE `world` = ? AND `chunkX`= ? AND `chunkZ` = ?";

	public synchronized void start() {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:./veya_chunk_store;create=true;MV_STORE=FALSE;");
			connection.createStatement().execute(CREATE_TABLE_STATEMENT);
			started = true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void stop() {
		try {
			started = false;
			connection.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void storeChunkStack(final World w, final ChunkStack stack) {
		if (!started) {
			return;
		}
		try {

			final PreparedStatement storeStatement = connection.prepareStatement(STORE_STATEMENT);

			storeStatement.setString(1, w.getName());
			storeStatement.setInt(2, stack.getX());
			storeStatement.setInt(3, stack.getZ());
			storeStatement.setBoolean(4, stack.isPopulated());
			storeStatement.setBytes(5, toSerialRepresentation(stack).toByteArray());

			storeStatement.executeUpdate();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		//System.out.println("STORED CHUNKSTACK " + stack);
	}

	public synchronized ChunkStack loadChunkStack(final World world, final int chunkX, final int chunkZ) {
		if (!started) {
			return null;
		}

		try {
			final PreparedStatement loadStatement = connection.prepareStatement(LOAD_STATEMENT);
			loadStatement.setString(1, world.getName());
			loadStatement.setInt(2, chunkX);
			loadStatement.setInt(3, chunkZ);

			final ResultSet resultSet = loadStatement.executeQuery();

			if (!resultSet.first()) {
				//System.out.println("LOADED CHUNKSTACK | " + +chunkX + " , " + chunkZ + " | " + null);
				return null;
			}

			final byte[] stackData = resultSet.getBytes(5);
			final boolean populated = resultSet.getBoolean(4);

			final ProtoDefinitions.ChunkStack protoStack = ProtoDefinitions.ChunkStack.parseFrom(stackData);
			final ChunkStack result = fromSerialRepresentation(protoStack, world, chunkX, chunkZ);

			result.setPopulated(populated);

			return result;
		} catch (final SQLException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ChunkStack fromSerialRepresentation(final ProtoDefinitions.ChunkStack protoStack, final World world, final int x, final int z) {
		final ChunkStack stack = new ChunkStack(world, x, z);
		for (final ProtoDefinitions.ChunkStack.Chunk protoChunk : protoStack.getChunkList()) {
			stack.setChunkAt(protoChunk.getY(), fromSerialRepresentation(protoChunk, world, x, z));
		}
		return stack;
	}

	private Chunk fromSerialRepresentation(final ProtoDefinitions.ChunkStack.Chunk protoChunk, final World world, final int chunkX, final int chunkZ) {
		final byte[] bytes = protoChunk.getData().toByteArray();

		final Chunk chunk = new Chunk(world, chunkX, protoChunk.getY(), chunkZ);
		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
					final byte value = bytes[x + y * Chunk.CHUNK_SIZE + z * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];
					if (value >= 0) {
						chunk.setBlockAt(x, y, z, BlockType.values()[value]);
					}
				}
			}
		}
		return chunk;
	}

	private ProtoDefinitions.ChunkStack toSerialRepresentation(final ChunkStack stack) {
		final List<ProtoDefinitions.ChunkStack.Chunk> protoChunks = new ArrayList<>();
		for (final Chunk chunk : stack.getChunks()) {
			protoChunks.add(toSerialRepresentation(chunk));
		}
		return ProtoDefinitions.ChunkStack.newBuilder().addAllChunk(protoChunks).build();
	}

	private ProtoDefinitions.ChunkStack.Chunk toSerialRepresentation(final Chunk chunk) {
		final byte[] bytes = new byte[Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE];

		for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
			for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
				for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
					byte value = -1;
					final BlockType type = chunk.getBlockAt(x, y, z);
					if (type != null) {
						value = (byte) type.ordinal();
					}
					bytes[x + y * Chunk.CHUNK_SIZE + z * Chunk.CHUNK_SIZE * Chunk.CHUNK_SIZE] = value;
				}
			}
		}

		final ByteString data = ByteString.copyFrom(bytes);
		return ProtoDefinitions.ChunkStack.Chunk.newBuilder().setY(chunk.getChunkY()).setData(data).build();
	}
}
