package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Log4j2
public class Parser extends RecursiveTask<ParseResults> {

  private static String mainUrl;
  private final ParseResults parseResults;

  private Parser(String url) {
    parseResults = new ParseResults(url);
  }

  public static Parser getInstance(String url) {
    if (!url.endsWith("/")) {
      url += "/";
    }
    mainUrl = url;
    ParseResults.getAlreadyParsed().clear();
    ParseResults.getAlreadyParsed().add(url);
    return new Parser(url);
  }

  @Override
  protected ParseResults compute() {
    try {
      getUrlsFromDocument(getDocumentFromCurrentUrl());
      if (parseResults.getUrlsSet().isEmpty()) {
        return parseResults;
      }
      ArrayList<Parser> parsers = new ArrayList<>();
      for (String str : parseResults.getUrlsSet()) {
        if (ParseResults.getAlreadyParsed().add(str)) {
          parsers.add(new Parser(str));
        }
      }
      invokeAll(parsers);
      return parseResults;
    } catch (IOException e) {
      log.error(e);
      return parseResults;
    } catch (InterruptedException e) {
      log.error(e);
      Thread.currentThread().interrupt();
      return parseResults;
    }
  }

  private Document getDocumentFromCurrentUrl() throws IOException,
      InterruptedException {
    Thread.sleep(new Random().nextInt(100) + 100);
    return Jsoup.connect(parseResults.getUrl()).maxBodySize(0).get();
  }

  private void getUrlsFromDocument(Document document) {
    for (Element e : document.select("a[href]")) {
      String absUrl = e.absUrl("href");
      if (absUrl.endsWith(".jpg") || absUrl.endsWith(".pdf") || absUrl
          .contains("#")
          || absUrl.endsWith(".png")) {
        continue;
      }
      if (!absUrl.endsWith("/")) {
        absUrl += "/";
      }
      if (!absUrl.equals(parseResults.getUrl()) && absUrl.startsWith(mainUrl)) {
        parseResults.getUrlsSet().add(absUrl);
      }
    }
  }

  public ParseResults getParseResults() {
    return parseResults;
  }
}
