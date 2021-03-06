package parser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import lombok.extern.log4j.Log4j2;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Log4j2
public class Parser extends RecursiveTask<ParseResults> {

  private static final List<String> foundUrlsCache =
      Collections.synchronizedList(new ArrayList<>());
  private static final DateTimeFormatter dtf =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
  private static final String UNNECESSARY_EXTENSION = ".+\\.(jpg|pdf|png)";
  private static final Map<String, ParseResults> allParseResults =
      new ConcurrentHashMap<>();
  private static String mainUrl;
  private final ParseResults parseResults;
  private static final byte DELAY = 100;

  Parser(String url) {
    parseResults = new ParseResults(url);
    allParseResults.put(url, parseResults);
  }

  public static Parser getInstance(String url) {
    if (!url.endsWith("/")) {
      url += "/";
    }
    mainUrl = url;
    allParseResults.clear();
    return new Parser(url);
  }

  public static Map<String, ParseResults> getAllParseResults() {
    return allParseResults;
  }

  @Override
  protected ParseResults compute() {
    try {
      getUrlsFromDocumentToParseResultUrlsSet(getDocumentFromCurrentUrl());
      if (parseResults.getUrlsSet().isEmpty()) {
        return parseResults;
      }
      ArrayList<Parser> parsers = new ArrayList<>();
      synchronized (parseResults.getUrlsSet()) {
        for (String str : parseResults.getUrlsSet()) {
          synchronized (allParseResults) {
            if (!allParseResults.containsKey(str)) {
              parsers.add(new Parser(str));
            }
          }
        }
      }
      invokeAll(parsers);
      return parseResults;
    } catch (IOException e) {
      log.error("", e);
      return parseResults;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return parseResults;
    }
  }

  private void getUrlsFromDocumentToParseResultUrlsSet(Document document) {
    for (Element e : document.select("a[href]")) {
      String absUrl = e.absUrl("href");
      if (absUrl.matches(UNNECESSARY_EXTENSION) || absUrl.contains("#")) {
        continue;
      }
      if (!absUrl.endsWith("/")) {
        absUrl += "/";
      }
      if (!absUrl.equals(parseResults.getUrl()) && absUrl.startsWith(mainUrl)) {
        parseResults.getUrlsSet().add(absUrl);
        foundUrlsCache
            .add("[" + LocalDateTime.now().format(dtf) + "]: " + absUrl);
      }
    }
  }

  public static List<String> getFoundUrlsCache() {
    return foundUrlsCache;
  }

  public ParseResults getParseResults() {
    return parseResults;
  }

  private Document getDocumentFromCurrentUrl() throws IOException,
      InterruptedException {
    Thread.sleep((long) DELAY + new Random().nextInt(DELAY));
    String url = parseResults.getUrl();
    try {
      return Jsoup.connect(url).maxBodySize(0).get();
    } catch (HttpStatusException ex) {
      log.error("Failed to load document from {}", url);
      return new Document("");
    }
  }
}
