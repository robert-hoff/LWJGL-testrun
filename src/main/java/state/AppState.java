package state;

import input.Action;

public interface AppState {
  boolean shutDown();
  void onAction(Action a);
}

