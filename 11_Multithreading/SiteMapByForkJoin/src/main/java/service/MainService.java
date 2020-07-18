package service;

import forms.SiteMapForm;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import parser.ParserService;
import utils.TextFileWriter;

@Log4j2
public class MainService {

  private static final String URL_REGEX =
      "^(https?://)?([\\da-z-.]+)\\.([a-z]{2,6})/?$";
  private static final int TIMER_DELAY = 1000;
  private static final String DONE_ERROR = "setTextWithParseResults (done "
      + "method) error: ";

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
    timer.startTimers();
  }

  private boolean checkInput(String inputURL) {
    if (inputURL.isEmpty() || !inputURL.matches(URL_REGEX)) {
      JOptionPane
          .showMessageDialog(siteMapForm.getMainPanel(),
              "Пожалуйста введите URL (example.com, http(s)://example.com)",
              "Ошибка",
              JOptionPane.ERROR_MESSAGE);
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
        JOptionPane
            .showMessageDialog(siteMapForm.getMainPanel(),
                "Парсинг успешно остановлен",
                "Сохраняем результаты в файл",
                JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      stopParsing();  //and then stop
    }).start();
  }

  public void stopParsing() {
    parserService.stopParser();
    String resultUrlsStr = timer.stopTimers();
    textFileWriter
        .saveResults(siteMapForm.getSiteURL().getText(), resultUrlsStr);
    siteMapForm.getBtnGetSitemap().setEnabled(true);
    siteMapForm.getSiteURL().setEnabled(true);
  }

  private class SiteMapFormTimer {

    private final Timer fromLaunchTimer;
    private final Timer resultTextTimer;
    private LocalDateTime start;
    private SwingWorker<Pair<String, Long>, Void> worker;

    SiteMapFormTimer() {
      fromLaunchTimer = new Timer(TIMER_DELAY, e -> setFromLaunchTime());
      resultTextTimer = new Timer(TIMER_DELAY, e -> setTextWithParseResults());
    }

    private void setFromLaunchTime() {
      siteMapForm.getLblElapsedTime().setText("Прошло времени с запуска "
          + "приложения: " +
          LocalTime
              .ofSecondOfDay(
                  start.until(LocalDateTime.now(), ChronoUnit.SECONDS))
              .format(DateTimeFormatter.ofPattern("mm:ss")));
    }

    private void setTextWithParseResults() {
      if (worker == null || worker.isDone()) {

        worker = new SwingWorker<>() {
          @Override
          protected Pair<String, Long> doInBackground() {
            String resultStr = parserService.getResults();
            Long resultsCount = resultStr.lines().count();
            return new ImmutablePair<>(resultStr, resultsCount);
          }

          @Override
          protected void done() {
            try {
              setFoundLinksText(get().getLeft(), get().getRight());
            } catch (InterruptedException interruptedException) {
              log.error(DONE_ERROR,
                  interruptedException);
              Thread.currentThread().interrupt();
            } catch (ExecutionException executionException) {
              log.error(DONE_ERROR,
                  executionException);
            }
          }
        };
        worker.execute();
      }
    }

    private void setFoundLinksText(String results, Long resultCount) {
      siteMapForm.getTxtDownloadLog().setText(results);
      siteMapForm.getLblFoundLinks()
          .setText("Найдено ссылок: " + resultCount);
    }

    private void startTimers() {
      start = LocalDateTime.now();
      fromLaunchTimer.start();
      resultTextTimer.start();
    }

    private String stopTimers() {
      fromLaunchTimer.stop();
      resultTextTimer.stop();
      setFromLaunchTime();
      String resultUrlsStr = parserService.getResults();
      Long urlsCount = resultUrlsStr.lines().count();
      setFoundLinksText(resultUrlsStr, urlsCount);
      return resultUrlsStr;
    }
  }
}
