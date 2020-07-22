package forms;

import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.Data;
import service.MainService;

@Data
public class SiteMapForm implements MyForm {

  private static final String PAUSE_BUTTON_ICON = "pause.png";
  private MainService mainService;

  private JPanel mainPanel;
  private JButton btnGetSitemap;
  private JTextArea txtDownloadLog;
  private JButton btnPause;
  private JButton btnStop;
  private JLabel lblElapsedTime;
  private JLabel lblFoundLinks;
  private JTextField siteURL;

  public SiteMapForm() {
    mainService = new MainService(this);
    btnPause.setIcon(
        new ImageIcon(
            Objects.requireNonNull(
                getClass().getClassLoader().getResource(PAUSE_BUTTON_ICON))));
    btnGetSitemap.addActionListener(e -> mainService.startParsing());
    btnStop.addActionListener(e -> mainService.stopParsing());
    btnPause.addActionListener(e -> mainService.doPauseOrResume());
  }

  @Override
  public JPanel getMainPanel() {
    return mainPanel;
  }
}
