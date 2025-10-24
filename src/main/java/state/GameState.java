package state;

import graphics.Camera;
import input.Action;

public class GameState implements AppState {
    private final Camera camera;
    private final Scene scene;
    private boolean orbiting;

    public GameState(Camera cam, Scene scn) {
    	this.camera = cam;
    	this.scene = scn;
    }

    public void onAction(Action a) {
        switch (a.type()) {
            case CLICK -> selectAt(a.x(), a.y());
            case DOUBLE_CLICK -> focusAt(a.x(), a.y());
            case ORBIT_START -> orbiting = true;
            case ORBIT_UPDATE -> { if (orbiting) camera.orbit(a.dx(), a.dy()); }
            case ORBIT_END -> orbiting = false;
            case DRAG_UPDATE -> maybeDragSelection(a.dx(), a.dy());
            case ZOOM -> camera.dolly(a.dy());
            default -> {}
        }
    }

    private void selectAt(double x, double y) { /* ray pick, set selection */ }
    private void focusAt(double x, double y) { /* fit camera to hit */ }
    private void maybeDragSelection(double dx, double dy) { /* translate gizmo */ }
}


