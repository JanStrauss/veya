package eu.over9000.veya;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.google.common.primitives.Ints;

import eu.over9000.veya.model.BlockType;
import eu.over9000.veya.model.Chunk;

public class ChunkVAO {

    private final boolean holdsTransparent;
    private final boolean holdsSolid;

    private int vbo_handle_solid;
    private int vbo_handle_transparent;

    private int vao_handle_solid;
    private int vao_handle_transparent;

    private int ibo_handle_solid;
    private int ibo_handle_transparent;

    private int index_length_solid;
    private int index_length_transparent;

    private IntBuffer ibo_buffer_solid;
    private IntBuffer ibo_buffer_transparent;

    private FloatBuffer vbo_buffer_solid;
    private FloatBuffer vbo_buffer_transparent;

    private final Program program;

    public ChunkVAO(final Chunk chunk, final Program program) {
        this.program = program;

        final int[] index_solid;
        final int[] index_transparent;

        final Vertex[] vertex_data_solid;
        final Vertex[] vertex_data_transparent;

        final List<Integer> indexListSolid = new ArrayList<>();
        final List<Integer> indexListTransparent = new ArrayList<>();

        final List<Vertex> vertexListSolid = new ArrayList<>();
        final List<Vertex> vertexListTransparent = new ArrayList<>();


        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    final BlockType block = chunk.getBlockAt(x, y, z);

                    if (block == null) {
                        continue;
                    }

                    final boolean solid = block.isSolid();

                    final int worldX = chunk.getChunkX() * Chunk.CHUNK_SIZE + x;
                    final int worldY = chunk.getChunkY() * Chunk.CHUNK_SIZE + y;
                    final int worldZ = chunk.getChunkZ() * Chunk.CHUNK_SIZE + z;

                    final BlockType neighborBlockBottom = chunk.getNeighborBlockBottom(x, y, z);
                    final BlockType neighborBlockTop = chunk.getNeighborBlockTop(x, y, z);
                    final BlockType neighborBlockNorth = chunk.getNeighborBlockNorth(x, y, z);
                    final BlockType neighborBlockSouth = chunk.getNeighborBlockSouth(x, y, z);
                    final BlockType neighborBlockWest = chunk.getNeighborBlockWest(x, y, z);
                    final BlockType neighborBlockEast = chunk.getNeighborBlockEast(x, y, z);

                    List<Vertex> vertexList = solid ? vertexListSolid : vertexListTransparent;
                    List<Integer> indexList = solid ? indexListSolid : indexListTransparent;


                    if (neighborBlockBottom == null || (solid && !neighborBlockBottom.isSolid())) {
                        ChunkVAO.addBottomOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }

                    if (neighborBlockTop == null || (solid && !neighborBlockTop.isSolid())) {
                        ChunkVAO.addTopOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }

                    if (neighborBlockNorth == null || (solid && !neighborBlockNorth.isSolid())) {
                        ChunkVAO.addNorthOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }

                    if (neighborBlockSouth == null || (solid && !neighborBlockSouth.isSolid())) {
                        ChunkVAO.addSouthOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }

                    if (neighborBlockWest == null || (solid && !neighborBlockWest.isSolid())) {
                        ChunkVAO.addWestOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }

                    if (neighborBlockEast == null || (solid && !neighborBlockEast.isSolid())) {
                        ChunkVAO.addEastOfBlock(indexList, vertexList, block, worldX, worldY, worldZ);
                    }


                }
            }
        }

        holdsSolid = !vertexListSolid.isEmpty();
        holdsTransparent = !vertexListTransparent.isEmpty();

        if (holdsSolid) {
            index_solid = Ints.toArray(indexListSolid);
            vertex_data_solid = vertexListSolid.toArray(new Vertex[vertexListSolid.size()]);

            this.index_length_solid = index_solid.length;
            this.ibo_buffer_solid = BufferUtils.createIntBuffer(index_solid.length);
            this.ibo_buffer_solid.put(index_solid);
            this.ibo_buffer_solid.flip();

            this.vbo_buffer_solid = BufferUtils.createFloatBuffer(vertex_data_solid.length * Vertex.elementCount);
            for (final Vertex vertex : vertex_data_solid) {
                this.vbo_buffer_solid.put(vertex.getElements());
            }
            this.vbo_buffer_solid.flip();
        }

        if (holdsTransparent) {
            index_transparent = Ints.toArray(indexListTransparent);
            vertex_data_transparent = vertexListTransparent.toArray(new Vertex[vertexListTransparent.size()]);

            this.index_length_transparent = index_transparent.length;

            this.ibo_buffer_transparent = BufferUtils.createIntBuffer(index_transparent.length);
            this.ibo_buffer_transparent.put(index_transparent);
            this.ibo_buffer_transparent.flip();

            this.vbo_buffer_transparent = BufferUtils.createFloatBuffer(vertex_data_transparent.length * Vertex.elementCount);
            for (final Vertex vertex : vertex_data_transparent) {
                this.vbo_buffer_transparent.put(vertex.getElements());
            }
            this.vbo_buffer_transparent.flip();
        }


    }

    public void create() {
        // create objects

        if (holdsSolid) {
            this.ibo_handle_solid = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_solid);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_buffer_solid, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            this.vbo_handle_solid = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_solid);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbo_buffer_solid, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

            this.vao_handle_solid = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(this.vao_handle_solid);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_solid);
            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexPosition"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexColor"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexTexturePosition"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexNormal"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_solid);

            GL30.glBindVertexArray(0);
        }

        if (holdsTransparent) {
            this.ibo_handle_transparent = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_transparent);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_buffer_transparent, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            this.vbo_handle_transparent = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_transparent);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vbo_buffer_transparent, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


            this.vao_handle_transparent = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(this.vao_handle_transparent);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo_handle_transparent);
            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexPosition"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexPosition"), Vertex.positionElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.positionByteOffset);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexColor"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexColor"), Vertex.colorElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.colorByteCount);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexTexturePosition"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexTexturePosition"), Vertex.textureElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.textureByteOffset);

            GL20.glEnableVertexAttribArray(this.program.getAttribLocation("vertexNormal"));
            GL20.glVertexAttribPointer(this.program.getAttribLocation("vertexNormal"), Vertex.normalElementCount, GL11.GL_FLOAT, false, Vertex.stride, Vertex.normalByteOffset);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo_handle_transparent);

            GL30.glBindVertexArray(0);
        }


        // System.out.println("created ChunkVAO with " + this.vertexData.length + " vertices");
    }

    public void render(boolean solid) {

        try {
            if (solid) {
                if (holdsSolid) {
                    GL30.glBindVertexArray(this.vao_handle_solid);
                    GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_solid, GL11.GL_UNSIGNED_INT, 0);
                    GL30.glBindVertexArray(0);
                }
            } else {
                if (holdsTransparent) {
                    GL30.glBindVertexArray(this.vao_handle_transparent);
                    GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, this.index_length_transparent, GL11.GL_UNSIGNED_INT, 0);
                    GL30.glBindVertexArray(0);
                }
            }
        } catch (final OpenGLException e) {
            System.out.println("vao_handle_solid: " + this.vao_handle_solid);
            e.printStackTrace();
        }

    }

    public void dispose() {

        if (holdsSolid) {

            GL30.glDeleteVertexArrays(this.vao_handle_solid);
            GL15.glDeleteBuffers(this.ibo_handle_solid);
            GL15.glDeleteBuffers(this.vbo_handle_solid);

            this.vao_handle_solid = -1;
            this.vbo_handle_solid = -1;
            this.ibo_handle_solid = -1;
        }

        if (holdsTransparent) {
            GL30.glDeleteVertexArrays(this.vao_handle_transparent);
            GL15.glDeleteBuffers(this.ibo_handle_transparent);
            GL15.glDeleteBuffers(this.vbo_handle_transparent);

            this.vao_handle_transparent = -1;
            this.vbo_handle_transparent = -1;
            this.ibo_handle_transparent = -1;
        }

    }

    private static void addBottomOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();
        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDBottom(), 0, -1, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDBottom(), 0, -1, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDBottom(), 0, -1, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDBottom(), 0, -1, 0));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    private static void addTopOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();

        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDTop(), 0, 1, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDTop(), 0, 1, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDTop(), 0, 1, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDTop(), 0, 1, 0));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    private static void addSouthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();

        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, block.getTextureIDSouth(), 0, 0, 1));
        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, block.getTextureIDSouth(), 0, 0, 1));
        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, block.getTextureIDSouth(), 0, 0, 1));
        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, block.getTextureIDSouth(), 0, 0, 1));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    private static void addNorthOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();

        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDNorth(), 0, 0, -1));
        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDNorth(), 0, 0, -1));
        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDNorth(), 0, 0, -1));
        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDNorth(), 0, 0, -1));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    private static void addWestOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();

        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDWest(), -1, 0, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDWest(), -1, 0, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDWest(), -1, 0, 0));
        vertexDataList.add(new Vertex(0.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDWest(), -1, 0, 0));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    private static void addEastOfBlock(final List<Integer> indexDataList, final List<Vertex> vertexDataList, final BlockType block, final int worldX, final int worldY, final int worldZ) {
        final int firstIndex = vertexDataList.size();

        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, block.getTextureIDEast(), 1, 0, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 1.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, block.getTextureIDEast(), 1, 0, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 0.0f + worldZ, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, block.getTextureIDEast(), 1, 0, 0));
        vertexDataList.add(new Vertex(1.0f + worldX, 0.0f + worldY, 1.0f + worldZ, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, block.getTextureIDEast(), 1, 0, 0));

        indexDataList.add(firstIndex + 0);
        indexDataList.add(firstIndex + 1);
        indexDataList.add(firstIndex + 2);
        indexDataList.add(firstIndex + 3);

        indexDataList.add(Veya.RESTART);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.ibo_handle_solid;
        result = prime * result + this.vao_handle_solid;
        result = prime * result + this.vbo_handle_solid;
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
        if (!(obj instanceof ChunkVAO)) {
            return false;
        }
        final ChunkVAO other = (ChunkVAO) obj;
        if (this.ibo_handle_solid != other.ibo_handle_solid) {
            return false;
        }
        if (this.vao_handle_solid != other.vao_handle_solid) {
            return false;
        }
        if (this.vbo_handle_solid != other.vbo_handle_solid) {
            return false;
        }
        return true;
    }

}
