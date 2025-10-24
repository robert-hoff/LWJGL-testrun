package input;

public record KeyEvent(long window, double timeSeconds, int key, int scancode, int action, int mods) implements InputEvent {}


