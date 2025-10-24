package input;


import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

class GestureRecognizer {
  private static final double CLICK_TIME = 0.20;   // seconds
  private static final double CLICK_DIST = 5.0;    // pixels
  private static final double DOUBLE_CLICK_TIME = 0.30;

  private boolean lDown = false;
  private double pressTime;
  private double px, py;      // press cursor
  private double lastClickTime = -10;

  private double cx, cy;      // current cursor
  private double dxAccum, dyAccum;

  List<Action> process(InputEvent e, List<Intent> intents) {
      var out = new ArrayList<Action>();
      if (e instanceof CursorEvent c) { dxAccum += c.x() - cx; dyAccum += c.y() - cy; cx = c.x(); cy = c.y(); }
      if (e instanceof MouseButtonEvent mb && mb.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
          if (mb.action() == GLFW.GLFW_PRESS) {
              lDown = true; pressTime = mb.timeSeconds(); px = cx; py = cy;
              if (intents.contains("ORBIT")) out.add(new Action(ActionType.ORBIT_START, cx, cy, mb.button(), mb.mods(), 0, 0));
          } else if (mb.action() == GLFW.GLFW_RELEASE) {
              boolean smallMove = Math.hypot(cx - px, cy - py) < CLICK_DIST;
              double dt = mb.timeSeconds() - pressTime;
              if (intents.contains("ORBIT")) out.add(new Action(ActionType.ORBIT_END, cx, cy, mb.button(), mb.mods(), 0, 0));
              if (smallMove && dt < CLICK_TIME) {
                  if (mb.timeSeconds() - lastClickTime < DOUBLE_CLICK_TIME) {
                      out.add(new Action(ActionType.DOUBLE_CLICK, cx, cy, mb.button(), mb.mods(), 0, 0));
                      lastClickTime = -10;
                  } else {
                      out.add(new Action(ActionType.CLICK, cx, cy, mb.button(), mb.mods(), 0, 0));
                      lastClickTime = mb.timeSeconds();
                  }
              } else {
                  out.add(new Action(ActionType.DRAG_END, cx, cy, mb.button(), mb.mods(), 0, 0));
              }
              lDown = false; dxAccum = dyAccum = 0;
          }
      }
      if (lDown && Math.hypot(cx - px, cy - py) >= CLICK_DIST) {
          out.add(new Action(ActionType.DRAG_UPDATE, cx, cy, GLFW.GLFW_MOUSE_BUTTON_LEFT, 0, dxAccum, dyAccum));
          dxAccum = dyAccum = 0;
      }
      if (e instanceof ScrollEvent s) {
          out.add(new Action(ActionType.ZOOM, cx, cy, -1, 0, 0, s.dy()));
      }
      return out;
  }

  List<Action> updateTime(double dt) { return List.of(); } // hook for time-based gestures if needed
}

