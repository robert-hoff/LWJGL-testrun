package input;

import java.util.Objects;

/**
 * A lightweight "intent" produced by {@link InputContext} from raw {@link InputEvent}s.
 * Intents are lower-level than gameplay/Editor Actions; they describe what the user
 * seems to be trying to do (e.g. startSelect, panCamera, rotateCamera, confirm, cancel).
 *
 * Your {@link GestureRecognizer} can then consume streams of Intents to produce
 * higher-level {@link Action}s (double-click, drag-select, long-press -> context menu, etc.).
 */
public final class Intent {
  public enum Type {
    POINTER_MOVE,
    POINTER_PRESS,
    POINTER_RELEASE,
    SCROLL,
    KEY_PRESS,
    KEY_RELEASE,
    COMMAND    // opaque, semantic command by name (e.g. "Undo", "Save", "TogglePlay")
  }

  private final Type type;
  private final String name;   // for COMMAND, or sub-type label like "Select", "Pan", "Rotate"
  private final double x;      // pointer X (if relevant)
  private final double y;      // pointer Y (if relevant)
  private final int code;      // key code / mouse button (if relevant)
  private final int mods;      // GLFW modifier bitfield (if relevant)

  private Intent(Type type, String name, double x, double y, int code, int mods) {
    this.type = Objects.requireNonNull(type);
    this.name = name;
    this.x = x;
    this.y = y;
    this.code = code;
    this.mods = mods;
  }

  public static Intent pointerMove(double x, double y, int mods) {
    return new Intent(Type.POINTER_MOVE, null, x, y, -1, mods);
  }
  public static Intent pointerPress(int button, double x, double y, int mods, String label) {
    return new Intent(Type.POINTER_PRESS, label, x, y, button, mods);
  }
  public static Intent pointerRelease(int button, double x, double y, int mods, String label) {
    return new Intent(Type.POINTER_RELEASE, label, x, y, button, mods);
  }
  public static Intent scroll(double dx, double dy, int mods) {
    // pack dx in x and dy in y (convention)
    return new Intent(Type.SCROLL, null, dx, dy, -1, mods);
  }
  public static Intent keyPress(int key, int mods, String label) {
    return new Intent(Type.KEY_PRESS, label, Double.NaN, Double.NaN, key, mods);
  }
  public static Intent keyRelease(int key, int mods, String label) {
    return new Intent(Type.KEY_RELEASE, label, Double.NaN, Double.NaN, key, mods);
  }
  public static Intent command(String name) {
    return new Intent(Type.COMMAND, name, Double.NaN, Double.NaN, -1, 0);
  }

  public Type type() { return type; }
  public String name() { return name; }
  public double x() { return x; }
  public double y() { return y; }
  public int code() { return code; }
  public int mods() { return mods; }

  @Override public String toString() {
    return "Intent{" + type +
        (name != null ? ", name=" + name : "") +
        ", x=" + x + ", y=" + y + ", code=" + code + ", mods=" + mods + "}";
  }
}
