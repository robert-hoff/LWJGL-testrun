package graphics;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 *
 * camera is defined by position, orientation and projection (aspect)
 *
 * projection parameters are independent from camera position and orientation
 * fovY, aspect, near, far
 *
 *
 */
public class Camera {

  public Vector3f position = new Vector3f(-3,3,-3);
  public float aspect = 6f/4;
  private float PI = (float) Math.PI;

  public Quaternionf rotation;
  // yaw rotates around the world Y axis (turning left/right)
  // double yaw = -140;
  // pitch rotates around the local X axis (looking up/down).
  // double pitch = -25;

  public Camera(float yaw, float pitch) {
    rotation = new Quaternionf().rotateY(yaw).rotateX(pitch);
  }

  public Matrix4f viewMat() {
    return new Matrix4f()
        .rotate(rotation.conjugate(new Quaternionf()))
        .translate(-position.x, -position.y, -position.z);
  }

  public Matrix4f projMat() {
    return new Matrix4f().perspective(PI/3, aspect, 0.1f, 1000f);
  }

  public Matrix4f viewProjMat() {
    // modifies projMat
    return projMat().mul(viewMat());
  }

  public void rotateAxisY(float deltaYaw) {
    float yaw = getYaw() + deltaYaw/200;
    float pitch = getPitch();
    rotation = new Quaternionf().rotateY(yaw).rotateX(pitch);
  }

  public void rotateAxisX(float deltaPitch) {
    float yaw = getYaw();
    float pitch = getPitch()+deltaPitch/200;
    rotation = new Quaternionf().rotateY(yaw).rotateX(pitch);
  }

  public void setOrientation(float yaw, float pitch) {
    rotation = new Quaternionf().rotateY(yaw).rotateX(pitch);
  }

  public void setPosition(float x, float y, float z) {
    position = new Vector3f(x,y,z);
  }

  // move backwards with negative dt
  public void moveForward(float dt) {
    Vector3f dir = forward();
    position.fma(dt, dir);
  }

  public void strafeRight(float dt) {
    Vector3f right = right();
    position.fma(dt, right);
  }

  public void moveUp(float dt) {
    position.fma(dt, new Vector3f(0f, 1f, 0f));
  }

  public Vector3f forward() {
    return rotation.transform(new Vector3f(0, 0, -1));
  }

  public Vector3f up() {
    return rotation.transform(new Vector3f(0, 1, 0));
  }

  public Vector3f right() {
    return rotation.transform(new Vector3f(1, 0, 0));
  }

  public void setAspect(int winWidth, int winHeight) {
    this.aspect = (float) winWidth / winHeight;
  }


  private final Vector3f angles = new Vector3f();

  public float getYaw() {
    return rotation.getEulerAnglesYXZ(angles).y;
  }

  public float getPitch() {
    return rotation.getEulerAnglesYXZ(angles).x;
  }

  public void updateAspect(int winWidth, int winHeight) {
    this.aspect = (float) winWidth / winHeight;
  }

  public void showYawPitchRoll() {
    Vector3f euler = new Vector3f();
    rotation.getEulerAnglesYXZ(euler);
    float yaw   = (float) Math.toDegrees(euler.y);
    float pitch = (float) Math.toDegrees(euler.x);
    float roll  = (float) Math.toDegrees(euler.z);
    System.out.printf("yaw=%5.3f pitch=%5.3f roll=%5.3f\n", yaw, pitch, roll);
  }

  public void showQuaternion() {
    System.out.printf("x=%6.3f y=%6.3f z=%6.3f w=%6.3f \n", rotation.x, rotation.y, rotation.z, rotation.w);
  }

  public void showPosition() {
    System.out.printf("x=%6.3f y=%6.3f z=%6.3f \n", rotation.x, rotation.y, rotation.z);
  }

}

