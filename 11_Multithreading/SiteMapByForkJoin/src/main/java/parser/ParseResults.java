package parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ParseResults implements Serializable {

  private static final Set<String> alreadyParsed = Collections
      .synchronizedSet(new HashSet<>());
  private static final Map<String, ParseResults> allParseResults =
      new ConcurrentHashMap<>();
  private final String url;
  private final HashSet<String> urlsSet = new HashSet<>();

  ParseResults(String url) {
    this.url = url;
    allParseResults.put(url, this);
  }

  public boolean isEmpty() {
    return urlsSet.isEmpty();
  }

  public static Set<String> getAlreadyParsed() {
    return alreadyParsed;
  }

  public String getUrl() {
    return url;
  }

  public Set<String> getUrlsSet() {
    return urlsSet;
  }

  public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
    if (i == 0) {
      stringBuilder.append(url);
    }
    for (String urlKey : urlsSet) {
      if (stringBuilder.indexOf(urlKey) >= 0) {
        continue;
      }
      stringBuilder.append("\r\n");
      stringBuilder.append("\t".repeat(i + 1));
      stringBuilder.append(urlKey);
      ParseResults parseResults = allParseResults.get(urlKey);
      if (parseResults != null) {
        int count = i;
        parseResults.toStringBuilder(stringBuilder, ++count);
      }
    }
    return stringBuilder;
  }
}
