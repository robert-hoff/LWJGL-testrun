package input;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import input.unused.InputContext;

/**
 * The {@code InputSystem} class manages user input events and converts them into high-level
 * {@link Action} instances that can be dispatched to the application.
 * <p>
 * It maintains an input queue, maps raw {@link InputEvent}s to logical intents using the
 * current {@link InputContext}, and processes gestures (e.g., double-clicks, long presses)
 * through a {@link GestureRecognizer}.
 * </p>
 */
public class InputSystem {
  
  /** Queue of pending input events awaiting processing. */
  private final ArrayDeque<InputEvent> queue = new ArrayDeque<>();
  
  /** Handles gesture recognition logic for time-based or multi-step input patterns. */
  private final EventHandler eventHandler = new EventHandler();
  
  /** The active input context, defining key or control mappings for the current mode. */
  // private InputContext context = InputContext.EDITOR_DEFAULT();
  
  /**
   * Adds a raw input event to the queue for later processing. Triggered by the the GLFW callbacks.
   *
   * @param e the {@link InputEvent} to enqueue
   */
  public void enqueue(InputEvent e) {
    queue.add(e);
  }
  
  
  
  /**
   * Updates the input system. This should be called once per frame from the main loop.
   * <p>
   * It processes (empties the events seen on each frame)
   * all queued input events, converts them into {@link Action}s, and dispatches
   * them using the provided consumer. It also updates gesture timers to handle
   * time-based gestures such as double-clicks or long-press actions.
   * </p>
   *
   * @param dt        the delta time (in seconds) since the last update
   * @param dispatch  a consumer used to handle or dispatch the resulting {@link Action}s
   *  the dispatch takes a function that takes 'Action' as argument and returns nothing.
   *  the function that is called is the onAction() defined in AppState, and implemented by
   *  GameState.
   *
   */
  public void update(double dt, Consumer<Action> dispatch) {
    // System.out.println(queue.size());
    while (!queue.isEmpty()) {
      var e = queue.pollFirst();
      for (Action a : toActions(e)) {
        dispatch.accept(a);
      }
    }
    // Update gesture recognizers for time-dependent behavior
    for (Action a : eventHandler.updateTime(dt)) {
      dispatch.accept(a);
    }
  }
  
  /**
   * Converts a raw input event into one or more high-level actions.
   * <p>
   * The conversion occurs in two stages:
   * <ol>
   *   <li>Mapping the raw input to low-level "intents" via the current input context.</li>
   *   <li>Passing these intents to the gesture recognizer to produce higher-level {@link Action}s.</li>
   * </ol>
   * </p>
   *
   * @param e the input event to translate
   * @return a list of resulting {@link Action}s
   */
  private List<Action> toActions(InputEvent e) {
    // remove InputContext for now, and pass all events to InputHandler instead
    return eventHandler.process(e);
  }
  
  /**
   * Sets the current input context, which defines the active control mappings.
   *
   * @param ctx the new {@link InputContext} to use
   */
  //  void setContext(InputContext ctx) {
  //    this.context = ctx;
  //  }
}

