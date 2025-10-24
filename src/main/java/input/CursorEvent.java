package input;

public record CursorEvent(long window, double timeSeconds, double x, double y) implements InputEvent {}

