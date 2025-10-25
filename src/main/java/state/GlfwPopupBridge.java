package state;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public final class GlfwPopupBridge {

  public final JPopupMenu popup;
  private final JWindow hostWindow;
  private final JPanel anchor;

  public GlfwPopupBridge(JPopupMenu popup) {
    if (popup == null) {
      throw new IllegalArgumentException("popup cannot be null");
    }
    this.popup = popup;

    // --- Build the tiny always-on-top, transparent Swing host ---
    hostWindow = new JWindow();
    hostWindow.setAlwaysOnTop(true);
    hostWindow.setBackground(new Color(0, 0, 0, 0));
    hostWindow.setSize(1, 1);

    anchor = new JPanel();
    anchor.setOpaque(false);
    hostWindow.setContentPane(anchor);
  }

  public void showAtScreen(int screenX, int screenY) {
    if (popup.isVisible()) {
      return;
    }
    SwingUtilities.invokeLater(() -> {
      // Ensure the host is "showing" before invoking popup.show(...)
      hostWindow.setLocation(screenX, screenY);
      if (!hostWindow.isVisible()) {
        hostWindow.setVisible(true);
        // Realize immediately to avoid location computations failing on some WMs
        hostWindow.toFront();
      }
      popup.show(anchor, 0, 0);
    });
  }

  public void hidePopup() {
    SwingUtilities.invokeLater(() -> {
      popup.setVisible(false);
      hostWindow.setVisible(false);
    });
  }

  /** Clean up: restore callbacks if you replaced them. */
  public void dispose() {
    SwingUtilities.invokeLater(() -> {
      popup.setVisible(false);
      hostWindow.setVisible(false);
      hostWindow.dispose();
    });
  }
}

