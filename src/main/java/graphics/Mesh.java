package graphics;
import static org.lwjgl.opengl.GL30.*;

public final class Mesh {
  public final int vao;
  public final int vbo;
  public final int vertexCount;

  private Mesh(int vao, int vbo, int vertexCount) {
    this.vao = vao;
    this.vbo = vbo;
    this.vertexCount = vertexCount;
  }

  public static Mesh create(float[] data) {
    final int VERTEX_FLOATS = 10;          // pos(3) + wobble(1) + normal(3) + color(3)
    final int STRIDE_BYTES  = VERTEX_FLOATS * Float.BYTES; // 40

    int vao = glGenVertexArrays();
    int vbo = glGenBuffers();

    glBindVertexArray(vao);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    // upload data
    glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

    // attribute layout (locations must match your vertex shader):
    // 0: position (vec3) at byte offset 0
    glVertexAttribPointer(0, 3, GL_FLOAT, false, STRIDE_BYTES, 0L);
    glEnableVertexAttribArray(0);

    // 1: wobble (float) at byte offset 12
    glVertexAttribPointer(1, 1, GL_FLOAT, false, STRIDE_BYTES, 12L);
    glEnableVertexAttribArray(1);

    // 2: normal (vec3) at byte offset 16
    glVertexAttribPointer(2, 3, GL_FLOAT, false, STRIDE_BYTES, 16L);
    glEnableVertexAttribArray(2);

    // 3: color (vec3) at byte offset 28
    glVertexAttribPointer(3, 3, GL_FLOAT, false, STRIDE_BYTES, 28L);
    glEnableVertexAttribArray(3);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    int vertexCount = data.length / VERTEX_FLOATS;
    return new Mesh(vao, vbo, vertexCount);
  }

  public void draw() {
    glBindVertexArray(vao);
    glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    glBindVertexArray(0);
  }

  public void dispose() {
    glDeleteBuffers(vbo);
    glDeleteVertexArrays(vao);
  }
}

