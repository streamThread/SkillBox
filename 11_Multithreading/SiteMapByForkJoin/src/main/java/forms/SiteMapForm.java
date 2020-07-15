package forms;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import utils.ParserService;

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

    btnGetSitemap.addActionListener(e -> {
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
      ParserService.getInstance().runParser(inputURL);
      timer.startTimers();
    });

    btnStop.addActionListener(e -> {
      ParserService.getInstance().stopParser();
      timer.stopTimers();
      txtDownloadLog.setText(ParserService.getInstance().getResults());
    });
  }

  @Override
  public JPanel getMainPanel() {
    return mainPanel;
  }

  private class MyTimer {

    private final Timer alreadyParsedTimer;
    private final Timer fromLaunchTimer;
    private final Timer resultTextTimer;
    private LocalTime start;
    boolean inProgress;


    MyTimer() {
      alreadyParsedTimer = new Timer(1000, e -> {
        int size = ParserService.getInstance().getParsedLinksCount();
        lblFoundLinks.setText("Найдено ссылок: " + size);
      });

      fromLaunchTimer = new Timer(1000,
          e -> lblElapsedTime.setText("Прошло времени с запуска приложения: " +
              LocalTime
                  .ofSecondOfDay(
                      start.until(LocalTime.now(), ChronoUnit.SECONDS))
                  .format(DateTimeFormatter.ofPattern("mm:ss"))));

      resultTextTimer = new Timer(1000, e -> {
        if (!inProgress) {
          SwingWorker worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
              inProgress = true;
              return ParserService.getInstance().getResults();
            }

            @Override
            protected void done() {
              try {
                txtDownloadLog.setText(get());
                inProgress = false;
              } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
              } catch (ExecutionException executionException) {
                executionException.printStackTrace();
              }
            }
          };
          worker.execute();
        }
      });
    }

    private void startTimers() {
      start = LocalTime.now();
      alreadyParsedTimer.start();
      fromLaunchTimer.start();
      resultTextTimer.start();
    }

    private void stopTimers() {
      alreadyParsedTimer.stop();
      fromLaunchTimer.stop();
      resultTextTimer.stop();
    }
  }
}
