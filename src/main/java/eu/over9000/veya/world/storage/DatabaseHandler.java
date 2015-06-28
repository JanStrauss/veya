package eu.over9000.veya.world.storage;

import java.io.*;
import java.sql.*;

import javax.sql.rowset.serial.SerialBlob;

import org.h2.util.IOUtils;

import eu.over9000.veya.world.BlockType;
import eu.over9000.veya.world.Chunk;
import eu.over9000.veya.world.World;

/**
 * Created by Jan on 28.06.2015.
 */
public class DatabaseHandler {

	private static Connection connection;

	private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS `chunks` (`world` VARCHAR(255), `chunkX` INTEGER , `chunkY` INTEGER , `chunkZ` INTEGER ,  `data` BLOB , PRIMARY KEY (`world`,`chunkX`,`chunkY`,`chunkZ`))";
	private static final String STORE_STATEMENT = "MERGE INTO `chunks` (`world`, `chunkX`, `chunkY`, `chunkZ`,  `data`) VALUES (?,?,?,?,?)";
	private static final String LOAD_STATEMENT = "SELECT * FROM `chunks` WHERE `world` = ? AND `chunkX`= ? AND `chunkY`= ? AND `chunkZ` = ?";

	public void start() {
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:./veya_chunk_store;create=true;MV_STORE=FALSE;");
			connection.createStatement().execute(CREATE_TABLE_STATEMENT);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		try {
			connection.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public void storeChunk(final World w, final Chunk chunk) {
		ObjectOutputStream objectOutputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(chunk.getBlocks());
			objectOutputStream.flush();

			final PreparedStatement storeStatement = connection.prepareStatement(STORE_STATEMENT);

			storeStatement.setString(1, w.getName());
			storeStatement.setInt(2, chunk.getChunkX());
			storeStatement.setInt(3, chunk.getChunkY());
			storeStatement.setInt(4, chunk.getChunkZ());
			storeStatement.setBlob(5, new SerialBlob(byteArrayOutputStream.toByteArray()));

			storeStatement.executeUpdate();
		} catch (final SQLException | IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeSilently(objectOutputStream);
			IOUtils.closeSilently(byteArrayOutputStream);
		}

		System.out.println("STORED CHUNK " + chunk);
	}

	public Chunk loadChunk(final World world, final int chunkX, final int chunkY, final int chunkZ) {
		ObjectInputStream objectInputStream = null;
		InputStream inputStream = null;
		try {
			final PreparedStatement loadStatement = connection.prepareStatement(LOAD_STATEMENT);
			loadStatement.setString(1, world.getName());
			loadStatement.setInt(2, chunkX);
			loadStatement.setInt(3, chunkY);
			loadStatement.setInt(4, chunkZ);

			final ResultSet resultSet = loadStatement.executeQuery();

			if (!resultSet.first()) {
				System.out.println("LOADED CHUNK | " + +chunkX + " | " + chunkY + " | " + chunkZ + " | " + null);
				return null;
			}

			inputStream = resultSet.getBlob(5).getBinaryStream();
			objectInputStream = new ObjectInputStream(inputStream);
			final BlockType[][][] data = (BlockType[][][]) objectInputStream.readObject();

			final Chunk chunk = new Chunk(world, chunkX, chunkY, chunkZ);

			for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
				for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
					for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
						chunk.setBlockAt(x, y, z, data[x][y][z]);
					}
				}
			}

			System.out.println("LOADED CHUNK | " + +chunkX + " | " + chunkY + " | " + chunkZ + " | " + chunk);
			return chunk;
		} catch (final SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			IOUtils.closeSilently(objectInputStream);
			IOUtils.closeSilently(inputStream);
		}
	}
}
