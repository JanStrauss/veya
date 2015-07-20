package eu.over9000.veya.world.storage;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

import eu.over9000.veya.proto.ProtoDefinitions;
import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;

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
		final BlockType[] data = new BlockType[Chunk.DATA_LENGTH];
		final byte[] loaded = protoChunk.getData().toByteArray();
		for (int i = 0; i < Chunk.DATA_LENGTH; i++) {
			final byte raw = loaded[i];
			if (raw >= 0) {
				data[i] = BlockType.values()[raw];
			}
		}
		return new Chunk(world, chunkX, protoChunk.getY(), chunkZ, data);
	}
	
	private ProtoDefinitions.ChunkStack toSerialRepresentation(final ChunkStack stack) {
		final List<ProtoDefinitions.ChunkStack.Chunk> protoChunks = stack.getChunks().stream().map(this::toSerialRepresentation).collect(Collectors.toList());
		return ProtoDefinitions.ChunkStack.newBuilder().addAllChunk(protoChunks).build();
	}
	
	private ProtoDefinitions.ChunkStack.Chunk toSerialRepresentation(final Chunk chunk) {
		final BlockType[] q = chunk.copyRaw();
		final byte[] toStore = new byte[Chunk.DATA_LENGTH];
		for (int i = 0; i < Chunk.DATA_LENGTH; i++) {
			final BlockType type = q[i];
			byte value = -1;
			if (type != null) {
				value = (byte) type.ordinal();
			}
			toStore[i] = value;
		}
		return ProtoDefinitions.ChunkStack.Chunk.newBuilder().setY(chunk.getChunkY()).setData(ByteString.copyFrom(toStore)).build();
	}
}
