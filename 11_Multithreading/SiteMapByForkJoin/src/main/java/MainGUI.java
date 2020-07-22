import forms.SiteMapForm;
import javax.swing.SwingUtilities;
import utils.MainFrame;

public class MainGUI {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> MainFrame.getInstance(new SiteMapForm()));
  }
}
