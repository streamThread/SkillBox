package util;

import forms.CollapseForm;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MyAppMainFrame extends JFrame {

  private static MyAppMainFrame frame;

  private MyAppMainFrame() {
    super("My App");
  }

  public static JFrame getInstance() {
    if (frame == null) {
      frame = new MyAppMainFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(new CollapseForm().getMainPanel());
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
      frame.pack();
      return frame;
    }
    return frame;
  }
}
