package state;

import input.Action;

public interface AppState {
    void onAction(Action a);
}
