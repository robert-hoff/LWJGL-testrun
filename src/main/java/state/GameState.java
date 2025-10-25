package state;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import graphics.Camera;
import input.Action;

public class GameState {

  private final String TITLE_DEFAULT = "Test App";
  private final int WIN_XPOS_DEFAULT = 500;
  private final int WIN_YPOS_DEFAULT = 50;
  private final int WIN_HEIGHT_DEFAULT = 800;
  private final int WIN_WIDTH_DEFAULT = 850;
  private final boolean SHOW_AXIS_DEFAULT = true;
  private final boolean SHOW_STATUS_TEXT_DEFAULT = true;

  // public final OrbitCamera camera = new OrbitCamera();
  public final Camera camera = new Camera();

  // private final Scene scene;
  private boolean orbiting;
  private boolean shutdown = false;

  public int winXPos;
  public int winYPos;
  public int winHeight;
  public int winWidth;
  public boolean showAxis;
  public boolean showStatusText;
  public String title = TITLE_DEFAULT;

  private final float DEFAULT_CAMERA_XROT = 10; // degrees
  private final float DEFAULT_CAMERA_YROT = -20;
  private final float[] DEFAULT_CAMERA_POS = {1.8f, 1f, 4f};

  private long glfwWindow;
  GlfwPopupBridge glfwBridge = new GlfwPopupBridge(getMiniPopup());

  public GameState(long glfwWindow) {
    this.glfwWindow = glfwWindow;
    // this.camera = camera;
    // this.scene = scene;

    ApplicationProp prop = new ApplicationProp();
    winXPos = prop.readInt("winXPos", WIN_XPOS_DEFAULT);
    winYPos = prop.readInt("winYPos", WIN_YPOS_DEFAULT);
    winHeight = prop.readInt("winHeight", WIN_HEIGHT_DEFAULT);
    winWidth = prop.readInt("winWidth", WIN_WIDTH_DEFAULT);
    showAxis = prop.readBoolean("showAxis", SHOW_AXIS_DEFAULT);
    showStatusText = prop.readBoolean("showStatusText", SHOW_STATUS_TEXT_DEFAULT);

    float xRot, yRot, cameraX, cameraY, cameraZ;
    xRot = prop.readFloat("xRot", DEFAULT_CAMERA_XROT);
    yRot = prop.readFloat("yRot", DEFAULT_CAMERA_YROT);
    cameraX = prop.readFloat("cameraX", DEFAULT_CAMERA_POS[0]);
    cameraY = prop.readFloat("cameraY", DEFAULT_CAMERA_POS[1]);
    cameraZ = prop.readFloat("cameraZ", DEFAULT_CAMERA_POS[2]);
    // cameraState = new CameraState(xRot,yRot,cameraX,cameraY,cameraZ);
  }


  boolean showCursor = true;

  public void onAction(Action a) {

    // System.out.println(a);

    switch (a.type()) {
      case CLICK -> {
        if (glfwBridge.popup.isVisible()) {
          glfwBridge.hidePopup();
        }
        selectAt(a.x(), a.y());
      }
      case DOUBLE_CLICK -> {
        focusAt(a.x(), a.y());
      }
      case ORBIT_START -> {
        orbiting = true;
      }
      case ORBIT_END -> {
        orbiting = false;
      }
      case MOUSE_MOVE -> {
        if (!showCursor) {
          camera.rotateAxisY(-a.dx());
          camera.rotateAxisX(-a.dy());
        }
      }
      case DRAG_UPDATE -> {
        // System.out.printf("a.dx=%5.3f a.dy=%5.3f \n", a.dx(), a.dy());
        camera.rotateAxisY(-a.dx());
        camera.rotateAxisX(-a.dy());
      }
      case ZOOM -> {
        camera.moveForward((float) a.dy());
      }
      case KEY -> {
        switch (a.val()) {
          case "1" -> {
            showCursor = !showCursor;
            if (showCursor) {
              GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            } else {
              GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            }
          }
          case "w" -> camera.moveForward(0.2f);
          case "s" -> camera.moveForward(-0.2f);
          case "a" -> camera.strafeRight(-0.2f);
          case "d" -> camera.strafeRight(0.2f);
          case "r" -> camera.moveUp(0.2f);
          case "f" -> camera.moveUp(-0.2f);
        }
      }
      case RIGHTCLICK -> {
        if (glfwBridge.popup.isVisible()) {
          glfwBridge.hidePopup();
        } else {
          glfwBridge.showAtScreen((int) a.x()+winXPos, (int) a.y()+winYPos);
        }
      }
      case SHUTDOWN -> {
        glfwBridge.dispose();
        this.shutdown = true;
      }
      default -> {}
    }
  }

  private void selectAt(double x, double y) { /* ray pick, set selection */ }
  private void focusAt(double x, double y) { /* fit camera to hit */ }
  private void maybeDragSelection(double dx, double dy) { /* translate gizmo */ }


  public void saveState(int winXPos, int winYPos, int winWidth, int winHeight) {
    this.winXPos = winXPos;
    this.winYPos = winYPos;
    this.winWidth = winWidth;
    this.winHeight = winHeight;
    savePropertiesToFile();
  }

  public void savePropertiesToFile() {
    ApplicationProp prop = new ApplicationProp();
    prop.addProperty("winXPos", ""+winXPos);
    prop.addProperty("winYPos", ""+winYPos);
    prop.addProperty("winWidth", ""+winWidth);
    prop.addProperty("winHeight", ""+winHeight);
    prop.addProperty("showAxis", ""+showAxis);
    prop.addProperty("showStatusText", ""+showStatusText);
    //    prop.addProperty("xRot", ""+cameraState.xRot);
    //    prop.addProperty("yRot", ""+cameraState.yRot);
    //    prop.addProperty("cameraX", ""+cameraState.cameraX);
    //    prop.addProperty("cameraY", ""+cameraState.cameraY);
    //    prop.addProperty("cameraZ", ""+cameraState.cameraZ);
    prop.saveToFile();
  }

  public int[] getWin() {
    return new int[] {winXPos, winYPos, winWidth, winHeight};
  }

  public void updateViewport(int winWidth, int winHeight) {
    this.winWidth = winWidth;
    this.winHeight = winHeight;
    camera.updateAspect(winWidth, winHeight);
  }

  public boolean shutDown() {
    return shutdown;
  }

  MiniPopup getMiniPopup() {
    MiniPopup menu = new MiniPopup();
    menu.addItem("Toggle Axes", e -> {
      // System.out.println("toggle");
      GameState.this.showAxis = !GameState.this.showAxis;
    });
    menu.addItem("Show quaternion", e -> {
      GameState.this.camera.showQuaternion();
    });
    menu.addItem("Show position", e -> {
      GameState.this.camera.showPosition();
    });
    menu.addItem("Show yaw/pitch/roll", e -> {
      camera.showYawPitchRoll();
    });
    menu.addItem("Show forward", e -> {
      Vector3f forw = camera.forward();
      System.out.printf("forward=(%5.3f,%5.3f,%5.3f) \n", forw.x, forw.y, forw.z);
    });
    menu.addItem("Show right", e -> {
      Vector3f right = camera.right();
      System.out.printf("right=(%5.3f,%5.3f,%5.3f) \n", right.x, right.y, right.z);
    });
    menu.addItem("Show up", e -> {
      Vector3f up = camera.up();
      System.out.printf("up=(%5.3f,%5.3f,%5.3f) \n", up.x, up.y, up.z);
    });
    return menu;
  }

  public void showWinDimensions() {
    System.out.printf("%d %d %d %d \n", winXPos, winYPos, winWidth, winHeight);
  }


}




