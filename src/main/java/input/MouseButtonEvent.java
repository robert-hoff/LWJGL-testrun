package input;

public record MouseButtonEvent(long window, double timeSeconds, int button, int action, int mods) implements InputEvent {}

