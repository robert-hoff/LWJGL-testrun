package input;

/**
 * The methods window() and timeSeconds() must be implemented by the inheriting classes,
 * or in this case records. Records implicitly adds this methods as getters. For example in
 *
 *    record KeyEvent(long window, double timeSeconds, int key, ...
 *
 *
 */
public sealed interface InputEvent permits KeyEvent, MouseButtonEvent, CursorEvent, ScrollEvent {
  long window();
  double timeSeconds();
}

