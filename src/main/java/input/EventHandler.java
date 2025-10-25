package input;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

public class EventHandler {
  private static final double CLICK_TIME = 0.20; // seconds
  private static final double CLICK_DIST = 5.0; // pixels
  private static final double DOUBLE_CLICK_TIME = 0.30;

  // Keyboard-driven motion amounts (in "pixel-like" units)
  private static final double KEY_PAN_STEP = 10.0;
  private static final double KEY_ZOOM_STEP = 1.0;

  private boolean lDown = false;
  private boolean orbitHeldByKeyboard = false; // ALT toggles orbit intent-like behavior

  private double pressTime;
  private double px, py; // press cursor
  private double lastClickTime = -10;

  private double cx, cy; // current cursor
  private double dxAccum, dyAccum;


  List<Action> process(InputEvent e) {
    // System.out.println(e);

    List<Action> out = new ArrayList<Action>();

    // Track cursor + accumulate deltas
    if (e instanceof CursorEvent c) {
      dxAccum += c.x() - cx;
      dyAccum += c.y() - cy;
      cx = c.x();
      cy = c.y();
    }
    // System.out.printf("dxAccum=%5.3f, dyAccum=%5.3f, cx=%5.3f, cy=%5.3f \n", dxAccum, dyAccum, cx, cy);


    // Mouse button (left)
    if (e instanceof MouseButtonEvent mb && mb.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

      // -- mouse button press
      if (mb.action() == GLFW.GLFW_PRESS) {
        lDown = true;
        pressTime = mb.timeSeconds();
        px = cx;
        py = cy;
        dxAccum = 0;
        dyAccum = 0;
      }

      // -- mouse button release
      if (mb.action() == GLFW.GLFW_RELEASE) {
        boolean smallMove = Math.hypot(cx - px, cy - py) < CLICK_DIST;
        double dt = mb.timeSeconds() - pressTime;
        if (smallMove && dt < CLICK_TIME) {
          if (mb.timeSeconds() - lastClickTime < DOUBLE_CLICK_TIME) {
            out.add(new Action(ActionType.DOUBLE_CLICK, cx, cy, mb.button(), mb.mods(), 0, 0, ""));
            lastClickTime = -10;
          } else {
            out.add(new Action(ActionType.CLICK, cx, cy, mb.button(), mb.mods(), 0, 0, ""));
            lastClickTime = mb.timeSeconds();
          }
        } else {
          out.add(new Action(ActionType.DRAG_END, cx, cy, mb.button(), mb.mods(), 0, 0, ""));
        }
        lDown = false;
        dxAccum = 0;
        dyAccum = 0;
      }
    }


    if (e instanceof MouseButtonEvent mb && mb.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
      if (mb.action() == GLFW.GLFW_PRESS) {
        mouseRightTime = mb.timeSeconds();
      }
      if (mb.action() == GLFW.GLFW_RELEASE) {
        // System.out.printf("cx=%5.3f, cy=%5.3f \n", cx, cy);
        if (mb.timeSeconds() - mouseRightTime < 0.4) {
          out.add(new Action(ActionType.RIGHTCLICK, cx, cy, 0, 0, 0, 0, ""));
        }
      }
    }


    // Mouse drag updates (while holding mouse down)
    if (lDown && Math.hypot(cx - px, cy - py) >= CLICK_DIST) {
      out.add(new Action(ActionType.DRAG_UPDATE, cx, cy, GLFW.GLFW_MOUSE_BUTTON_LEFT, 0, dxAccum, dyAccum, ""));
      dxAccum = 0;
      dyAccum = 0;
      return out;
    }

    // add mouse move in case camera in FP mode
    if (e instanceof CursorEvent c) {
      out.add(new Action(ActionType.MOUSE_MOVE, cx, cy, 0, 0, dxAccum, dyAccum, ""));
      dxAccum = 0;
      dyAccum = 0;
    }



    // Mouse wheel => zoom
    if (e instanceof ScrollEvent s) {
      out.add(new Action(ActionType.ZOOM, cx, cy, -1, 0, 0, s.dy(), ""));
    }

    // --- Keyboard handling ---
    if (e instanceof KeyEvent k) {
      final int key = k.key();
      final int key_action = k.action();
      final int mods = k.mods();

      // ALT down/up => orbit start/end (with synthetic "button" -1)
      if (key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT) {
        if (key_action == GLFW.GLFW_PRESS) {
          if (!orbitHeldByKeyboard) {
            orbitHeldByKeyboard = true;
            out.add(new Action(ActionType.ORBIT_START, cx, cy, -1, mods, 0, 0, ""));
          }
        } else if (key_action == GLFW.GLFW_RELEASE) {
          if (orbitHeldByKeyboard) {
            orbitHeldByKeyboard = false;
            out.add(new Action(ActionType.ORBIT_END, cx, cy, -1, mods, 0, 0, ""));
          }
        }
      }

      // Arrow keys => small pan steps (emit DRAG_UPDATE pulses)
      // if (key_action == GLFW.GLFW_PRESS || key_action == GLFW.GLFW_REPEAT) {
      if (key_action == GLFW.GLFW_PRESS || key_action == GLFW.GLFW_REPEAT) {
        double kdx = 0;
        double kdy = 0;
        if (key == GLFW.GLFW_KEY_LEFT) {
          kdx = -KEY_PAN_STEP;
        }
        if (key == GLFW.GLFW_KEY_RIGHT) {
          kdx =  KEY_PAN_STEP;
        }
        if (key == GLFW.GLFW_KEY_UP) {
          kdy = -KEY_PAN_STEP; // up is negative y (screen coords)
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
          kdy =  KEY_PAN_STEP;
        }
        if (kdx != 0 || kdy != 0) {
          out.add(new Action(ActionType.DRAG_UPDATE, cx, cy, -1, mods, kdx, kdy, ""));
        }
        // Keyboard zoom: PageUp/PageDown and +/- keys
        if (key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_EQUAL || key == GLFW.GLFW_KEY_KP_ADD) {
          out.add(new Action(ActionType.ZOOM, cx, cy, -1, mods, 0, -KEY_ZOOM_STEP, ""));
        }
        if (key == GLFW.GLFW_KEY_PAGE_DOWN || key == GLFW.GLFW_KEY_MINUS || key == GLFW.GLFW_KEY_KP_SUBTRACT) {
          out.add(new Action(ActionType.ZOOM, cx, cy, -1, mods, 0,  KEY_ZOOM_STEP, ""));
        }
        // ESC => cancel drag if in progress
        //        if (key == GLFW.GLFW_KEY_ESCAPE && lDown) {
        //          out.add(new Action(ActionType.DRAG_END, cx, cy, GLFW.GLFW_MOUSE_BUTTON_LEFT, mods, 0, 0));
        //          lDown = false; dxAccum = dyAccum = 0;
        //        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
          out.add(new Action(ActionType.SHUTDOWN, 0, 0, 0, 0, 0, 0, ""));
        }
        if (key == GLFW.GLFW_KEY_1) {
          dxAccum = 0;
          dyAccum = 0;
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "1"));
        }
        if (key == GLFW.GLFW_KEY_W) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "w"));
        }
        if (key == GLFW.GLFW_KEY_S) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "s"));
        }
        if (key == GLFW.GLFW_KEY_A) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "a"));
        }
        if (key == GLFW.GLFW_KEY_D) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "d"));
        }
        if (key == GLFW.GLFW_KEY_R) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "r"));
        }
        if (key == GLFW.GLFW_KEY_F) {
          out.add(new Action(ActionType.KEY, 0, 0, 0, 0, 0, 0, "f"));
        }
      }
    }

    return out;
  }



  double mouseRightTime = 0;

  //hook for time-based gestures if needed
  List<Action> updateTime(double dt) {
    return List.of();
  }

}







