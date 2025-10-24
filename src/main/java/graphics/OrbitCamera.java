package graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrbitCamera {
  // Camera state
  private final Vector3f target = new Vector3f(0, 0, 0); // point to orbit
  private float radius = 6.0f;       // distance from target (zoom control)
  private float yaw   = 0.0f;        // radians, around Y
  private float pitch = (float) Math.toRadians(20); // radians, up/down

  // Limits
  private float minRadius = 1.0f;
  private float maxRadius = 50.0f;
  private float minPitch  = (float) Math.toRadians(-89);
  private float maxPitch  = (float) Math.toRadians( 89);

  // Projection params
  private float fovYRadians = (float) Math.toRadians(60);
  private float near = 0.1f, far = 200.0f;

  // Cached matrices
  private final Matrix4f view = new Matrix4f();
  private final Matrix4f proj = new Matrix4f();
  private final Matrix4f viewProj = new Matrix4f();

  /** Scroll wheel zoom: positive yOffset = zoom in, negative = out */
  public void zoom(float yOffset) {
    // scale zoom speed with distance so it feels nice
    float zoomSpeed = Math.max(0.1f, radius * 0.1f);
    radius -= yOffset * zoomSpeed;
    radius = Math.max(minRadius, Math.min(maxRadius, radius));
  }

  /** Optional: change yaw/pitch (e.g., mouse drag) */
  public void rotate(float deltaYaw, float deltaPitch) {
    yaw   += deltaYaw;
    pitch += deltaPitch;
    if (pitch < minPitch) {
          pitch = minPitch;
        }
    if (pitch > maxPitch) {
          pitch = maxPitch;
        }
  }

  /** Optional: pan the target (e.g., middle-mouse drag) */
  public void pan(Vector3f delta) {
    target.add(delta);
  }

  /** Recompute view matrix from orbit params */
  public Matrix4f getView() {
    // spherical → cartesian
    float cosPitch = (float) Math.cos(pitch);
    float sinPitch = (float) Math.sin(pitch);
    float cosYaw   = (float) Math.cos(yaw);
    float sinYaw   = (float) Math.sin(yaw);

    Vector3f eye = new Vector3f(
        target.x + radius * cosPitch * sinYaw,
        target.y + radius * sinPitch,
        target.z + radius * cosPitch * cosYaw
        );

    return view.identity().lookAt(eye, target, new Vector3f(0,1,0));
  }

  /** Set/update projection given window size */
  public Matrix4f getProjection(int width, int height) {
    float aspect = Math.max(1f, (float) width / Math.max(1, height));
    return proj.identity().perspective(fovYRadians, aspect, near, far);
  }

  /** Convenience: directly get View * Projection */
  public Matrix4f getViewProj(int width, int height) {
    return viewProj.set(getProjection(width, height)).mul(getView());
  }

  // Getters/setters for customization
  public void setTarget(float x, float y, float z) { target.set(x,y,z); }
  public void setRadius(float r) { radius = Math.max(minRadius, Math.min(maxRadius, r)); }
  public float getRadius() { return radius; }
  public void setYawPitch(float newYaw, float newPitch) {
    yaw = newYaw; pitch = Math.max(minPitch, Math.min(maxPitch, newPitch));
  }
  public void setFovDegrees(float fovDeg) { fovYRadians = (float) Math.toRadians(fovDeg); }
  public void setClip(float near, float far) { this.near = near; this.far = far; }
}



