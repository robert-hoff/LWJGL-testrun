package input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.lwjgl.glfw.GLFW.*;

/**
 * InputContext maps raw {@link InputEvent}s to a list of semantic {@link Intent}s,
 * based on the active "mode". Think: Editor mode, Gameplay mode, Text-entry mode, etc.
 *
 * Usage:
 *   var ctx = InputContext.builder()
 *       .onMouse(GLFW_MOUSE_BUTTON_LEFT).press().emit("Select")
 *       .onMouse(GLFW_MOUSE_BUTTON_MIDDLE).drag().emit("Pan")
 *       .onMouse(GLFW_MOUSE_BUTTON_RIGHT).drag().emit("Rotate")
 *       .onScroll().emitZoom()     // turns scroll into COMMAND("ZoomIn/Out")
 *       .onKey(GLFW_KEY_W).press().emit("MoveForward")
 *       .onChord(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_Z).emitCommand("Undo")
 *       .build();
 *
 *   // Provide a handy default:
 *   InputContext EDITOR_DEFAULT = InputContext.EDITOR_DEFAULT();
 */
public interface InputContext {

    /** Map one raw event into zero or more {@link Intent}s. */
    List<Intent> map(InputEvent e);

    /** Replace with whatever "default editor" you want. */
    static InputContext EDITOR_DEFAULT() {
        return builder()
            // ---- Pointer basics ----
            .onMouse(GLFW_MOUSE_BUTTON_LEFT).press().emit("Select")       // LMB press -> POINTER_PRESS "Select"
            .onMouse(GLFW_MOUSE_BUTTON_LEFT).release().emit("Select")     // LMB release -> POINTER_RELEASE "Select"
            .onMouse(GLFW_MOUSE_BUTTON_LEFT).drag().emit("DragSelect")    // LMB drag -> POINTER_MOVE labeled "DragSelect"
            .onMouse(GLFW_MOUSE_BUTTON_MIDDLE).drag().emit("Pan")         // MMB drag -> camera pan
            .onMouse(GLFW_MOUSE_BUTTON_RIGHT).drag().emit("Rotate")       // RMB drag -> orbit
            .onScroll().emitZoom()                                        // scroll -> COMMAND("ZoomIn/Out")
            .onCursorMove().emitMove()                                    // raw pointer move passthrough
            // ---- Common editor keys ----
            .onKey(GLFW_KEY_DELETE).press().emitCommand("Delete")
            .onKey(GLFW_KEY_BACKSPACE).press().emitCommand("Delete")
            .onChord(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_Z).emitCommand("Undo")
            .onChord(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_Y).emitCommand("Redo")
            .onChord(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_S).emitCommand("Save")
            .onKey(GLFW_KEY_ESCAPE).press().emitCommand("Cancel")
            .onKey(GLFW_KEY_SPACE).press().emitCommand("TogglePlay")
            // ---- WASD fly ----
            .onKey(GLFW_KEY_W).press().emit("MoveForward")
            .onKey(GLFW_KEY_S).press().emit("MoveBack")
            .onKey(GLFW_KEY_A).press().emit("MoveLeft")
            .onKey(GLFW_KEY_D).press().emit("MoveRight")
            .build();
    }

    /** Builder entry point. */
    static Builder builder() { return new Builder(); }

    // ------------------------- Implementation -------------------------

    final class Builder {
        private final List<Rule> rules = new ArrayList<>();

        public MouseRuleBuilder onMouse(int button) { return new MouseRuleBuilder(rules, button); }
        public ScrollRuleBuilder onScroll() { return new ScrollRuleBuilder(rules); }
        public CursorRuleBuilder onCursorMove() { return new CursorRuleBuilder(rules); }
        public KeyRuleBuilder onKey(int key) { return new KeyRuleBuilder(rules, key); }
        public ChordRuleBuilder onChord(int modifierKey, int key) { return new ChordRuleBuilder(rules, modifierKey, key); }

        public InputContext build() { return new DefaultContext(List.copyOf(rules)); }
    }

    final class DefaultContext implements InputContext {
        private final List<Rule> rules;
        DefaultContext(List<Rule> rules) { this.rules = rules; }

        @Override public List<Intent> map(InputEvent e) {
            var out = new ArrayList<Intent>(2);
            for (Rule r : rules) r.tryEmit(e, out);
            return out;
        }
    }

    // ------------------------- Rules & Builders -------------------------

    abstract class Rule {
        final Predicate<InputEvent> when;
        Rule(Predicate<InputEvent> when) { this.when = when; }
        abstract void tryEmit(InputEvent e, List<Intent> out);
    }

    
    final class MouseRule extends Rule {
        final int button;
        final Phase phase;
        final String label;
        MouseRule(Predicate<InputEvent> when, int button, Phase phase, String label) {
            super(when);
            this.button = button;
            this.phase = phase;
            this.label = label;
        }
        @Override void tryEmit(InputEvent e, List<Intent> out) {
            if (!(e instanceof MouseButtonEvent mbe)) return;
            if (mbe.button() != button) return;
            if (!when.test(e)) return;

            if (phase == Phase.PRESS && mbe.action() == GLFW_PRESS) {
                out.add(Intent.pointerPress(button, Double.NaN, Double.NaN, mbe.mods(), label));
            } else if (phase == Phase.RELEASE && mbe.action() == GLFW_RELEASE) {
                out.add(Intent.pointerRelease(button, Double.NaN, Double.NaN, mbe.mods(), label));
            } else if (phase == Phase.DRAG && mbe.action() == GLFW_REPEAT) {
                // Convention: send labeled move intents during drags
                out.add(Intent.pointerMove(Double.NaN, Double.NaN, mbe.mods()));
            }
        }
    }

    final class CursorRule extends Rule {
        CursorRule(Predicate<InputEvent> when) { super(when); }
        @Override void tryEmit(InputEvent e, List<Intent> out) {
            if (e instanceof CursorEvent ce && when.test(e)) {
                out.add(Intent.pointerMove(ce.x(), ce.y(), 0));
            }
        }
    }

    final class ScrollRule extends Rule {
        ScrollRule(Predicate<InputEvent> when) { super(when); }
        @Override void tryEmit(InputEvent e, List<Intent> out) {
            if (e instanceof ScrollEvent se && when.test(e)) {
                out.add(Intent.scroll(se.dx(), se.dy(), 0));
            }
        }
    }

    final class KeyRule extends Rule {
        final int key;
        final Phase phase;
        final String label;
        KeyRule(Predicate<InputEvent> when, int key, Phase phase, String label) {
            super(when);
            this.key = key;
            this.phase = phase;
            this.label = label;
        }
        @Override void tryEmit(InputEvent e, List<Intent> out) {
            if (!(e instanceof KeyEvent ke)) return;
            if (ke.key() != key) return;
            if (!when.test(e)) return;

            if (phase == Phase.PRESS && ke.action() == GLFW_PRESS) {
                out.add(Intent.keyPress(ke.key(), ke.mods(), label));
            } else if (phase == Phase.RELEASE && ke.action() == GLFW_RELEASE) {
                out.add(Intent.keyRelease(ke.key(), ke.mods(), label));
            }
        }
    }

    final class ChordRule extends Rule {
        final int modKey;
        final int key;
        final String command;
        ChordRule(Predicate<InputEvent> when, int modKey, int key, String command) {
            super(when);
            this.modKey = modKey;
            this.key = key;
            this.command = command;
        }
        @Override void tryEmit(InputEvent e, List<Intent> out) {
            if (!(e instanceof KeyEvent ke)) return;
            if (ke.key() != key) return;
            if (!when.test(e)) return;

            boolean ctrlHeld = (ke.mods() & (GLFW_MOD_CONTROL)) != 0;
            boolean leftCtrl = ke.scancode() == glfwGetKeyScancode(modKey);
            if ((ctrlHeld || leftCtrl) && ke.action() == GLFW_PRESS) {
                out.add(Intent.command(command));
            }
        }
    }

    enum Phase { PRESS, RELEASE, DRAG }

    
    
    // ------------------------- Tiny DSL builders -------------------------

    abstract class RuleBuilder<B extends RuleBuilder<B>> {
        final List<Rule> out;
        Predicate<InputEvent> pred = e -> true;
        RuleBuilder(List<Rule> out) { this.out = out; }
        @SuppressWarnings("unchecked") B when(Predicate<InputEvent> p) { pred = pred.and(p); return (B) this; }
    }

    public static final class MouseRuleBuilder extends RuleBuilder<MouseRuleBuilder> {
        private final int button;
        private Phase phase = Phase.PRESS;
        private String label = "Pointer";
        MouseRuleBuilder(List<Rule> out, int button) { super(out); this.button = button; }
        public MouseRuleBuilder press() { this.phase = Phase.PRESS; return this; }
        public MouseRuleBuilder release() { this.phase = Phase.RELEASE; return this; }
        public MouseRuleBuilder drag() { this.phase = Phase.DRAG; return this; }
        public MouseRuleBuilder label(String label) { this.label = Objects.requireNonNull(label); return this; }
        
        
        public Builder emit(String label) {
          out.add(new MouseRule(pred, button, phase, label));
          {
            Builder __b = new Builder();
            __b.rules.addAll(out);
            return __b;
          }            
        }
    }

    
    public static final class CursorRuleBuilder extends RuleBuilder<CursorRuleBuilder> {
        CursorRuleBuilder(List<Rule> out) { super(out); }
        public Builder emitMove() { out.add(new CursorRule(pred));
          {
            Builder __b = new Builder();
            __b.rules.addAll(out);
            return __b;
          }           
        }
    }

    public static final class ScrollRuleBuilder extends RuleBuilder<ScrollRuleBuilder> {
        ScrollRuleBuilder(List<Rule> out) { super(out); }
        public Builder emitZoom() {
            // Interpret dy>0 as zoom-in, dy<0 as zoom-out in GestureRecognizer; here we just emit raw scroll
            out.add(new ScrollRule(pred)); 
            {
              Builder __b = new Builder();
              __b.rules.addAll(out);
              return __b;
            }            
        }
    }

    public static final class KeyRuleBuilder extends RuleBuilder<KeyRuleBuilder> {
        private final int key;
        private Phase phase = Phase.PRESS;
        private String label = "Key";
        KeyRuleBuilder(List<Rule> out, int key) { super(out); this.key = key; }
        public KeyRuleBuilder press() { this.phase = Phase.PRESS; return this; }
        public KeyRuleBuilder release() { this.phase = Phase.RELEASE; return this; }
        public KeyRuleBuilder label(String label) { this.label = Objects.requireNonNull(label); return this; }
        public Builder emit(String label) {
          out.add(new KeyRule(pred, key, phase, label));
          {
            Builder __b = new Builder();
            __b.rules.addAll(out);
            return __b;
          }   
        }
        public Builder emitCommand(String command) { out.add(new KeyRule(pred, key, Phase.PRESS, command));
        {
          Builder __b = new Builder();
          __b.rules.addAll(out);
          return __b;
        }   
        }
    }

    public static final class ChordRuleBuilder extends RuleBuilder<ChordRuleBuilder> {
        private final int modKey;
        private final int key;
        ChordRuleBuilder(List<Rule> out, int modKey, int key) { super(out); this.modKey = modKey; this.key = key; }
        public Builder emitCommand(String command) {
          out.add(new ChordRule(pred, modKey, key, command));
          {
            Builder __b = new Builder();
            __b.rules.addAll(out);
            return __b;
          }   
        }
    }
}

