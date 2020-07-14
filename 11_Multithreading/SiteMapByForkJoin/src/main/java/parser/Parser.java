package parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Log4j2
public class Parser extends RecursiveTask<Parser.ParseResults> {

  private static final Set<String> alreadyParsed = Collections
      .synchronizedSet(new HashSet<>());
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
    alreadyParsed.clear();
    alreadyParsed.add(url);
    return new Parser(url);
  }

  public static Set<String> getAlreadyParsed() {
    return alreadyParsed;
  }

  @Override
  protected ParseResults compute() {

    Document document;

    try {
      Thread.sleep(new Random().nextInt(100) + 100);
      document = Jsoup.connect(parseResults.URL).maxBodySize(0).get();
    } catch (IOException e) {
      log.error(e);
      return parseResults;
    } catch (InterruptedException e) {
      log.error(e);
      Thread.currentThread().interrupt();
      return parseResults;
    }

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
      if (!absUrl.equals(parseResults.URL) && absUrl.startsWith(mainUrl)) {
        parseResults.urlsSet.add(absUrl);
      }
    }

    ArrayList<Parser> parsers = new ArrayList<>();

    if (!parseResults.urlsSet.isEmpty()) {

      for (String str : parseResults.urlsSet) {
        if (alreadyParsed.add(str)) {
          parsers.add(new Parser(str));
        }
      }
    } else {
      return parseResults;
    }

    for (Parser parser : parsers) {
      parser.fork();
    }

    for (Parser parser : parsers) {
      ParseResults joinParseResults = parser.join();
      if (joinParseResults.isEmpty()) {
        continue;
      }
      parseResults.parseResultsMap
          .put(parser.parseResults.URL, joinParseResults);
    }

    return parseResults;
  }

  public ParseResults getParseResults() {
    return parseResults;
  }

  public class ParseResults {

    private final String URL;
    private final HashSet<String> urlsSet = new HashSet<>();
    private final Map<String, ParseResults> parseResultsMap = new HashMap<>();

    private ParseResults(String url) {
      URL = url;
    }

    public boolean isEmpty() {
      return parseResultsMap.isEmpty() && urlsSet.isEmpty();
    }

    public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
      if (i == 0) {
        stringBuilder.append(URL);
      }
      Iterator<String> iterator = urlsSet.iterator();
      while (iterator.hasNext()) {
        String urlKey = iterator.next();
        stringBuilder.append("\r\n");
        for (int j = 0; j <= i; j++) {
          stringBuilder.append("\t");
        }
        stringBuilder.append(urlKey);
        ParseResults parseResults = parseResultsMap.get(urlKey);
        if (parseResults != null) {
          int count = i;
          parseResults.toStringBuilder(stringBuilder, ++count);
        }
      }
      return stringBuilder;
    }

    public Map<String, ParseResults> getParseResultsMap() {
      return parseResultsMap;
    }
  }
}
