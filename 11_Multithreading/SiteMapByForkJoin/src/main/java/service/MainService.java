package service;

import forms.SiteMapForm;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import lombok.extern.log4j.Log4j2;
import parser.ParserService;
import utils.TextFileWriter;

@Log4j2
public class MainService {

  private static final String URL_REGEX =
      "^(https?://)?([\\da-z-.]+)\\.([a-z]{2,6})/?$";
  private static final int TIMER_DELAY = 1000;
  private static final String DONE_ERROR =
      "setTextWithParseResults (done method) error: ";

  private boolean isPaused;

  private final SiteMapForm siteMapForm;
  private final SiteMapFormTimer timer = new SiteMapFormTimer();
  private final ParserService parserService = new ParserService();
  private final TextFileWriter textFileWriter = new TextFileWriter();

  public MainService(SiteMapForm siteMapForm) {
    this.siteMapForm = siteMapForm;
  }

  public void startParsing() {
    String inputURL = siteMapForm.getSiteURL().getText();
    if (!checkInput(inputURL)) {
      return;
    }
    runParserInNewThread(inputURL);
    siteMapForm.getBtnGetSitemap().setEnabled(false);
    siteMapForm.getSiteURL().setEnabled(false);
    siteMapForm.getBtnPause().setEnabled(true);
    siteMapForm.getBtnStop().setEnabled(true);
    timer.startTimers();
  }

  private boolean checkInput(String inputURL) {
    if (inputURL.isEmpty() || !inputURL.matches(URL_REGEX)) {
      showErrorMessage();
      return false;
    }
    return true;
  }

  private void runParserInNewThread(String inputURL) {
    new Thread(() -> {
      try {
        //invoke parser and wait until parser work done
        parserService.runParser(inputURL);
      } catch (CancellationException ex) {
        return;
      }
      stopParsing();  //and then stop
    }).start();
  }

  public void doPauseOrResume() {
    if (isPaused) {
      resumeParsing();
    } else {
      pauseParsing();
    }
  }

  private void pauseParsing() {
    isPaused = true;
    showPauseMessage();
    parserService.stopParser();
    timer.stopTimers();
    timer.pause = LocalDateTime.now();
  }

  private void resumeParsing() {
    parserService.resumeParser();
    timer.startTimers();
    isPaused = false;
  }

  public void stopParsing() {
    showStopMessage();
    if (!isPaused) {
      parserService.stopParser();
      timer.stopTimers();
    }
    textFileWriter
        .saveResults(siteMapForm.getSiteURL().getText(),
            parserService.getResultString());
    siteMapForm.getBtnGetSitemap().setEnabled(true);
    siteMapForm.getSiteURL().setEnabled(true);
    siteMapForm.getBtnStop().setEnabled(false);
    siteMapForm.getBtnPause().setEnabled(false);
    isPaused = false;
  }

  private void showStopMessage() {
    SwingUtilities.invokeLater(() -> JOptionPane
        .showMessageDialog(siteMapForm.getMainPanel(),
            "Парсинг успешно завершен",
            "Сохраняем результаты в файл",
            JOptionPane.INFORMATION_MESSAGE));
  }

  private void showPauseMessage() {
    SwingUtilities.invokeLater(() -> JOptionPane
        .showMessageDialog(siteMapForm.getMainPanel(),
            "Парсинг приостановлен. "
                + "Для возобновления нажмите на ▶",
            "Пауза",
            JOptionPane.INFORMATION_MESSAGE));
  }

  private void showErrorMessage() {
    SwingUtilities.invokeLater(() -> JOptionPane
        .showMessageDialog(siteMapForm.getMainPanel(),
            "Пожалуйста введите URL (example.com, http(s)://example.com)",
            "Ошибка",
            JOptionPane.ERROR_MESSAGE));
  }

  private class SiteMapFormTimer {

    private final Timer fromLaunchTimer;
    private final Timer resultTextTimer;
    private final Timer resultsCountTimer;
    private LocalDateTime start;
    private LocalDateTime pause;
    private SwingWorker<Long, Void> foundUrlsCountWorker;

    SiteMapFormTimer() {
      fromLaunchTimer = new Timer(TIMER_DELAY, e -> setFromLaunchTime());
      resultTextTimer = new Timer(0, e -> setTextWithParseResults());
      resultsCountTimer = new Timer(TIMER_DELAY, e -> setFoundUrlsCount());
    }

    private void setFromLaunchTime() {
      siteMapForm.getLblElapsedTime().setText("Прошло времени с запуска "
          + "парсера: " +
          LocalTime
              .ofSecondOfDay(
                  start.until(LocalDateTime.now(), ChronoUnit.SECONDS))
              .format(DateTimeFormatter.ofPattern("mm:ss")));
    }

    private void setFoundUrlsCount() {
      if (foundUrlsCountWorker != null && !foundUrlsCountWorker.isDone()) {
        return;
      }
      foundUrlsCountWorker = new SwingWorker<>() {
        @Override
        protected Long doInBackground() {
          return parserService.getFoundLinksCount();
        }

        @Override
        protected void done() {
          try {
            siteMapForm.getLblFoundLinks()
                .setText("Найдено ссылок: " + get());
          } catch (InterruptedException interruptedException) {
            log.error(DONE_ERROR, interruptedException);
            Thread.currentThread().interrupt();
          } catch (ExecutionException executionException) {
            log.error(DONE_ERROR, executionException);
          }
        }
      };
      foundUrlsCountWorker.execute();
    }

    private void setTextWithParseResults() {
      siteMapForm.getTxtDownloadLog().setText(
          parserService.getFirstFewFoundLinks(25));
    }

    private void startTimers() {
      if (isPaused) {
        start = LocalDateTime.now().minus(
            timer.start.until(timer.pause,
                ChronoUnit.SECONDS), ChronoUnit.SECONDS);
      } else {
        start = LocalDateTime.now();
      }
      resultsCountTimer.start();
      fromLaunchTimer.start();
      resultTextTimer.start();
    }

    private void stopTimers() {
      resultsCountTimer.stop();
      fromLaunchTimer.stop();
      resultTextTimer.stop();
      setTextWithParseResults();
      setFromLaunchTime();
      setFoundUrlsCount();
    }
  }
}
