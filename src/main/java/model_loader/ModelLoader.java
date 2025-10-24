package model_loader;


// import org.lwjgl.util.vector.Vector3f;
import org.joml.Vector3f;

public class ModelLoader {

  public static float[] loadModel(CSVReader reader, float size) {
    reader.nextLine();
    int dataCount = reader.getNextInt() * MemorySlot.VERTEX_FLOAT_COUNT;
    int sectionsCount = reader.getNextInt();
    float[] data = new float[dataCount];
    int pointer = 0;
    for (int k = 0; k < sectionsCount; k++) {
      reader.nextLine();
      int vertexCount = reader.getNextInt();
      Vector3f colour = reader.getNextVector();
      float wobbleFactor = 0;
      if(!reader.isEndOfLine()){
        wobbleFactor = reader.getNextFloat();
      }
      reader.nextLine();
      for (int i = 0; i < vertexCount; i++) {
        Vector3f position = reader.getNextVector();
        data[pointer++] = position.x * size;
        data[pointer++] = position.y * size;
        data[pointer++] = position.z * size;
        data[pointer++] = position.y * size * wobbleFactor;
        for (int j = 0; j < 3; j++) {// loading normals
          data[pointer++] = reader.getNextFloat();
        }
        data[pointer++] = colour.x;
        data[pointer++] = colour.y;
        data[pointer++] = colour.z;
      }
    }
    return data;
  }

}


