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

  public Quaternionf rotation;
  // yaw rotates around the world Y axis (turning left/right)
  // double yaw = -140;
  // pitch rotates around the local X axis (looking up/down).
  // double pitch = -25;

  public Camera(double yaw, double pitch) {
    rotation = new Quaternionf()
        .rotateY((float) Math.toRadians(yaw))
        .rotateX((float) Math.toRadians(pitch));
  }

  public Matrix4f viewMat() {
    return new Matrix4f()
        .rotate(rotation.conjugate(new Quaternionf()))
        .translate(-position.x, -position.y, -position.z);
  }

  public Matrix4f projMat() {
    Matrix4f projMatrix = new Matrix4f()
        .perspective((float)Math.toRadians(60.0f), aspect, 0.1f, 1000f);
    return projMatrix;
  }

  public Matrix4f viewProjMat() {
    // modifies projMat
    return projMat().mul(viewMat());
  }

  public void rotateAxisY(double deltaYaw) {
    double yaw = getYaw() + deltaYaw/5;
    double pitch = getPitch();
    rotation = new Quaternionf()
        .rotateY((float) Math.toRadians(yaw))
        .rotateX((float) Math.toRadians(pitch));
  }

  public void rotateAxisX(double deltaPitch) {
    double yaw = getYaw();
    double pitch = getPitch()+deltaPitch/5;
    rotation = new Quaternionf()
        .rotateY((float) Math.toRadians(yaw))
        .rotateX((float) Math.toRadians(pitch));
  }

  public void setOrientation(double yaw, double pitch) {
    rotation = new Quaternionf()
        .rotateY((float) Math.toRadians(yaw))
        .rotateX((float) Math.toRadians(pitch));
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

  public float getYaw() {
    Vector3f euler = new Vector3f();
    rotation.getEulerAnglesYXZ(euler);
    float yaw   = (float) Math.toDegrees(euler.y);
    return yaw;
  }

  public float getPitch() {
    Vector3f euler = new Vector3f();
    rotation.getEulerAnglesYXZ(euler);
    float pitch = (float) Math.toDegrees(euler.x);
    return pitch;
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

