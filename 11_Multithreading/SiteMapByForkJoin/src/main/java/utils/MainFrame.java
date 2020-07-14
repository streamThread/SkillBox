package utils;

import forms.MyForm;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {

  private static MainFrame frame;

  private MainFrame() {
    super("Site Map app");
  }

  public static MainFrame getInstance(MyForm form) {
    if (frame == null) {
      frame = new MainFrame();
      frame.setContentPane(form.getMainPanel());
      frame.pack();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    }
    return frame;
  }
}
