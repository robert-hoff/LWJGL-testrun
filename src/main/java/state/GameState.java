package state;

import org.lwjgl.glfw.GLFW;
import graphics.OrbitCamera;
import input.Action;

public class GameState {

  private final String TITLE_DEFAULT = "Test App";
  private final int WIN_XPOS_DEFAULT = 500;
  private final int WIN_YPOS_DEFAULT = 50;
  private final int WIN_HEIGHT_DEFAULT = 800;
  private final int WIN_WIDTH_DEFAULT = 850;
  private final boolean SHOW_AXIS_DEFAULT = true;
  private final boolean SHOW_STATUS_TEXT_DEFAULT = true;

  public final OrbitCamera camera = new OrbitCamera();
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
    // glfwBridge.attachTo(window);
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


  boolean showCursor = false;

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
      // case ORBIT_UPDATE -> { if (orbiting) { camera.orbit(a.dx(), a.dy()); } }
      case ORBIT_END -> {
        orbiting = false;
      }
      case DRAG_UPDATE -> {
        camera.rotate((float) -a.dx() * 0.005f, (float) a.dy() * 0.005f);
      }
      case ZOOM -> {
        // camera.dolly(a.dy());
        camera.zoom((float) a.dy());
      }
      case KEY -> {
        showCursor = !showCursor;
        if (showCursor) {
          GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
          GLFW.glfwSetInputMode(glfwWindow, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
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

  public boolean shutDown() {
    return shutdown;
  }

  MiniPopup getMiniPopup() {
    MiniPopup menu = new MiniPopup();
    menu.addItem("Toggle Axes",        e -> {
      // System.out.println("toggle");
      GameState.this.showAxis = !GameState.this.showAxis;
    });
    //    menu.addItem("Toggle status text", e -> {
    //      System.out.println("status text");
    //    });
    //    menu.addSeparator();
    //    menu.addItem("Set Scale",          e -> {
    //      System.out.println("set scale");
    //    });
    //    menu.addItem("Clear status info",  e -> {
    //      System.out.println("clear info");
    //    });
    menu.addItem("Show matrices", e -> {
      System.out.println("show camera and view matrices");
    });
    //    menu.addSeparator();
    //    menu.addItem("Start",              e -> {
    //      System.out.println("start animation");
    //    });
    //    menu.addItem("Stop",               e -> {
    //      System.out.println("stop animation");
    //    });
    //    menu.addItem("Reset",              e -> {
    //      System.out.println("restart animation");
    //    });
    return menu;
  }

  public void showWinDimensions() {
    System.out.printf("%d %d %d %d \n", winXPos, winYPos, winWidth, winHeight);
  }


}




