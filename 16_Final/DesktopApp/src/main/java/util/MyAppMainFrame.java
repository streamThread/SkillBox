package util;

import forms.CollapseForm;
import forms.MyForm;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MyAppMainFrame extends JFrame {

  private static MyAppMainFrame frame;

  private MyAppMainFrame() {
    super("My App");
  }

  public static MyAppMainFrame getInstance() {
    if (frame == null) {
      frame = new MyAppMainFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setLocationRelativeTo(null);
      frame.changeMainForm(new CollapseForm());
      frame.setVisible(true);
      return frame;
    }
    return frame;
  }

  public void changeMainForm(MyForm form) {
    frame.setContentPane(form.getContentPanel());
    frame.pack();
    frame.repaint();
    frame.revalidate();
  }
}
