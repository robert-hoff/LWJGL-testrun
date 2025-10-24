package input;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Consumer;

public class InputSystem {
  private final ArrayDeque<InputEvent> queue = new ArrayDeque<>();
  private final GestureRecognizer gestures = new GestureRecognizer();
  private InputContext context = InputContext.EDITOR_DEFAULT(); // current mapping/mode

  public void enqueue(InputEvent e) { queue.add(e); }

  /** Called once per frame from your main loop */
  public void update(double dt, Consumer<Action> dispatch) {
      while (!queue.isEmpty()) {
          var e = queue.pollFirst();
          for (Action a : toActions(e)) dispatch.accept(a);
      }
      // allow time-based gesture timeouts (double-click window, long-press, etc.)
      for (Action a : gestures.updateTime(dt)) dispatch.accept(a);
  }

  private List<Action> toActions(InputEvent e) {
      // 1) Map raw events to preliminary “intent” via context bindings
      var intents = context.map(e);
      // 2) Feed intents into gesture FSMs to produce higher-level Actions
      return gestures.process(e, intents);
  }

  void setContext(InputContext ctx) { this.context = ctx; }
}

