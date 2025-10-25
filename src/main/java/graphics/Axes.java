package graphics;
import static org.lwjgl.opengl.GL30.*;

public final class Axes {
  public final int vao;

  private Axes(int vao) {
    this.vao = vao;
  }

  public static Axes create() {
    int axisVao = glGenVertexArrays();
    return new Axes(axisVao);
  }

  public void draw() {
    glBindVertexArray(vao);
    glDrawArrays(GL_LINES, 0, 12);  // 12 vertices = 6 lines
    glBindVertexArray(0);
  }

  public void dispose() {
    glDeleteVertexArrays(vao);
  }
}
