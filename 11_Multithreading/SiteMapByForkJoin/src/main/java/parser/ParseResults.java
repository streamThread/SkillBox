package parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

public class ParseResults implements Serializable {

  private static final Map<String, ParseResults> allParseResults =
      new ConcurrentHashMap<>();
  private final String url;
  private final Set<String> urlsSet = Collections
      .synchronizedSet(new HashSet<>());

  ParseResults(String url) {
    this.url = url;
    allParseResults.put(url, this);
  }

  public boolean isEmpty() {
    return urlsSet.isEmpty();
  }

  public String getUrl() {
    return url;
  }

  public Set<String> getUrlsSet() {
    return urlsSet;
  }

  public static Map<String, ParseResults> getAllParseResults() {
    return allParseResults;
  }

  public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
    if (i == 0) {
      stringBuilder.append(url);
    }
    synchronized (urlsSet) {
      for (String urlKey : urlsSet) {
        stringBuilder.append("\r\n").append("\t".repeat(i + 1)).append(urlKey);
        if (StringUtils.countMatches(stringBuilder.toString(), urlKey) == 1) {
          ParseResults parseResults = allParseResults.get(urlKey);
          if (parseResults != null) {
            parseResults.toStringBuilder(stringBuilder, i + 1);
          }
        }
      }
    }
    return stringBuilder;
  }
}
