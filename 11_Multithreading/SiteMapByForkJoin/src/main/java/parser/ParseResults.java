package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ParseResults {

  private static final Set<String> alreadyParsed = Collections
      .synchronizedSet(new HashSet<>());
  private final String url;
  private final HashSet<String> urlsSet = new HashSet<>();
  private final Map<String, ParseResults> parseResultsMap = new HashMap<>();

  ParseResults(String url) {
    this.url = url;
  }

  public static Set<String> getAlreadyParsed() {
    return alreadyParsed;
  }

  public boolean isEmpty() {
    return parseResultsMap.isEmpty() && urlsSet.isEmpty();
  }

  public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
    if (i == 0) {
      stringBuilder.append(url);
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

  public String getUrl() {
    return url;
  }

  public HashSet<String> getUrlsSet() {
    return urlsSet;
  }
}
