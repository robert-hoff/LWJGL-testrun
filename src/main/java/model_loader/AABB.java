package model_loader;

// import org.lwjgl.util.vector.Vector3f;
// import org.lwjgl.util.vector.Vector4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class AABB {

  private Vector3f scale;
  private Vector4f offset;

  public AABB(Vector3f min, Vector3f max) {
    // this.scale = Vector3f.sub(max, min, null);
    this.scale = new Vector3f(max).sub(min);
    // Vector3f middle = Vector3f.add(min, new Vector3f(scale.x/2f, scale.y/2f, scale.z/2f), null);
    Vector3f middle = new Vector3f(min).lerp(max, 0.5f);
    // this.offset = new Vector4f(middle.x, middle.y, middle.z, 1f);
    this.offset = new Vector4f(middle, 1f);
  }

  public Vector3f getScale() {
    return scale;
  }

  public float getHeight(){
    return scale.y * 0.5f + offset.y;
  }

  public Vector4f getOffset() {
    return offset;
  }

  public float getMaxDimension(){
    return Math.max(scale.y, getMaxWidth());
  }

  public float getMaxWidth(){
    return Math.max(scale.x, scale.z);
  }

}


