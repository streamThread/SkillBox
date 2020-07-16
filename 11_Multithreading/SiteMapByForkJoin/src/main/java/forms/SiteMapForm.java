package forms;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.CancellationException;
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
import parser.ParserService;

public class SiteMapForm implements MyForm {

  private static final String URL_REGEX =
      "^(https?://)?([\\da-z-.]+)\\.([a-z]{2,6})/?$";

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
    btnGetSitemap.addActionListener(e -> startParser());
    btnStop.addActionListener(e -> stopParser());
  }

  @Override
  public JPanel getMainPanel() {
    return mainPanel;
  }

  private void startParser() {
    String inputURL = siteURL.getText();
    if (inputURL.isEmpty() || !inputURL.matches(URL_REGEX)) {
      JOptionPane
          .showMessageDialog(mainPanel,
              "Пожалуйста введите URL (example.com, http(s)://example.com)",
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
      return;
    }
    new Thread(() -> {
      try {
        ParserService.getInstance().runParser(inputURL);
      } catch (CancellationException ex) {
      }
      stopParser();
    }).start();

    timer.startTimers();
  }

  private void stopParser() {
    ParserService.getInstance().stopParser();
    timer.stopTimers();
    txtDownloadLog.setText(ParserService.getInstance().getResults());
  }

  private class MyTimer {

    private final Timer alreadyParsedTimer;
    private final Timer fromLaunchTimer;
    private final Timer resultTextTimer;
    private LocalDateTime start;
    private SwingWorker<String, Void> worker;


    MyTimer() {
      alreadyParsedTimer = new Timer(1000, e -> {
        int size = ParserService.getInstance().getParsedLinksCount();
        lblFoundLinks.setText("Найдено ссылок: " + size);
      });

      fromLaunchTimer = new Timer(1000,
          e -> lblElapsedTime.setText("Прошло времени с запуска приложения: " +
              LocalTime
                  .ofSecondOfDay(
                      start.until(LocalDateTime.now(), ChronoUnit.SECONDS))
                  .format(DateTimeFormatter.ofPattern("mm:ss"))));

      resultTextTimer = new Timer(1000, e -> {
        if (worker == null || worker.isDone()) {
          worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
              return ParserService.getInstance().getResults();
            }

            @Override
            protected void done() {
              try {
                txtDownloadLog.setText(get());
              } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
                Thread.currentThread().interrupt();
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
      start = LocalDateTime.now();
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
