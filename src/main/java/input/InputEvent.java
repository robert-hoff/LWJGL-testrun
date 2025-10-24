package input;

sealed interface InputEvent permits KeyEvent, MouseButtonEvent, CursorEvent, ScrollEvent {
  long window();
  double timeSeconds();
}





