package graphics;
import static org.lwjgl.opengl.GL20.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;

public class Shader {
  private final int programId;

  public Shader(String vertexSource, String fragmentSource) {
    int vs = compileShader(GL_VERTEX_SHADER, vertexSource);
    int fs = compileShader(GL_FRAGMENT_SHADER, fragmentSource);

    programId = glCreateProgram();
    glAttachShader(programId, vs);
    glAttachShader(programId, fs);
    glLinkProgram(programId);

    // check link status
    if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
      throw new RuntimeException("Shader link failed: " + glGetProgramInfoLog(programId));
    }

    // we can detach & delete the individual shaders after linking
    glDetachShader(programId, vs);
    glDetachShader(programId, fs);
    glDeleteShader(vs);
    glDeleteShader(fs);
  }

  private static int compileShader(int type, String src) {
    int id = glCreateShader(type);
    glShaderSource(id, src);
    glCompileShader(id);
    if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));
    }
    return id;
  }

  public void bind() {
    glUseProgram(programId);
  }

  public void unbind() {
    glUseProgram(0);
  }

  public void dispose() {
    glDeleteProgram(programId);
  }

  // convenience uniform setters
  public void set(String name, float value) {
    int loc = glGetUniformLocation(programId, name);
    glUniform1f(loc, value);
  }

  public void set(String name, Matrix4f mat) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer fb = stack.mallocFloat(16);
      mat.get(fb);
      int loc = glGetUniformLocation(programId, name);
      glUniformMatrix4fv(loc, false, fb);
    }
  }

  public void set(String name, org.joml.Vector3f vec) {
    int loc = glGetUniformLocation(programId, name);
    glUniform3f(loc, vec.x, vec.y, vec.z);
  }

  public int getId() {
    return programId;
  }
}
