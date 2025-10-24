package input;

public record ScrollEvent(long window, double timeSeconds, double dx, double dy) implements InputEvent {}

