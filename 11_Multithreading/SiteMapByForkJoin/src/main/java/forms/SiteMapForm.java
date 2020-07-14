package forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import utils.MainFormService;

public class SiteMapForm implements MyForm {

  private static final String URL_REGEX =
      "^(https:\\/\\/)?([\\da-z\\.-]+)\\.([a-z]{2,6})\\/?$";

  private final MyTimer timer = new MyTimer();

  private JPanel mainPanel;
  private JButton btnGetSitemap;
  private JTextArea txtDownloadLog;
  private JButton btnPause;
  private JButton btnStop;
  private JLabel lblElapsedTime;
  private JLabel lblFoundLinks;
  private JTextField siteURL;


  public SiteMapForm() {
    btnPause.setIcon(new ImageIcon(Objects
        .requireNonNull(getClass().getClassLoader().getResource("pause.png"))));
    btnGetSitemap.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String inputURL = siteURL.getText();
        if (inputURL.isEmpty() || !inputURL.matches(URL_REGEX)) {
          JOptionPane
              .showMessageDialog(mainPanel,
                  "Пожалуйста введите URL (example.com, https://example.com)",
                  "Ошибка",
                  JOptionPane.ERROR_MESSAGE);
          return;
        }
        if (!inputURL.startsWith("https://")) {
          inputURL = String.format("https://%s", inputURL);
        }
        MainFormService.getInstance().runParser(inputURL);
        timer.startTimers();
      }
    });
    btnStop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        MainFormService.getInstance().stopParser();
        timer.stopTimers();
        txtDownloadLog.setText(MainFormService.getInstance().getResults());
      }
    });
  }

  @Override
  public JPanel getMainPanel() {
    return mainPanel;
  }

  private class MyTimer {

    private final Timer alreadyParsedTimer;
    private final Timer fromLaunchTimer;
    private final Timer resultsTimer;
    private LocalTime start;

    MyTimer() {
      alreadyParsedTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int size = MainFormService.getInstance().getParsedLinksCount();
          lblFoundLinks.setText("Найдено ссылок: " + size);
        }
      });
      fromLaunchTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          lblElapsedTime.setText("Прошло времени с запуска приложения: " +
              LocalTime
                  .ofSecondOfDay(
                      start.until(LocalTime.now(), ChronoUnit.SECONDS))
                  .format(DateTimeFormatter.ofPattern("mm:ss")));
        }
      });
      resultsTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          txtDownloadLog.setText(MainFormService.getInstance().getResults());
        }
      });
    }

    private void startTimers() {
      start = LocalTime.now();
      alreadyParsedTimer.start();
      fromLaunchTimer.start();
      resultsTimer.start();
    }

    private void stopTimers() {
      alreadyParsedTimer.stop();
      fromLaunchTimer.stop();
      resultsTimer.stop();
    }
  }
}
