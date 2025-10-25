package state;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.*;

public class MiniPopup extends JPopupMenu {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final List<JMenuItem> items = new ArrayList<>();
  private final Map<String, JMenuItem> byText = new HashMap<>();

  public MiniPopup() {

  }

  public void addItem(String label, ActionListener l) {
    JMenuItem item = new JMenuItem(label);
    if (l != null) {
      item.addActionListener(l);
    }
    add(item);
    items.add(item);
    byText.put(label, item);
  }
}

