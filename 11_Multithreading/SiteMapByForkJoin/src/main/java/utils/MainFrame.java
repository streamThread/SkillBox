package utils;

import static utils.MainFrame.MainFrameHolder.MAIN_FRAME_INSTANCE;

import forms.MyForm;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {

  private MainFrame() {
    super("Site Map app");
  }

  public static MainFrame getInstance(MyForm form) {
    MAIN_FRAME_INSTANCE.setContentPane(form.getMainPanel());
    MAIN_FRAME_INSTANCE.pack();
    MAIN_FRAME_INSTANCE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    MAIN_FRAME_INSTANCE.setLocationRelativeTo(null);
    MAIN_FRAME_INSTANCE.setVisible(true);
    return MAIN_FRAME_INSTANCE;
  }

  public static class MainFrameHolder {

    public static final MainFrame MAIN_FRAME_INSTANCE = new MainFrame();
  }
}
