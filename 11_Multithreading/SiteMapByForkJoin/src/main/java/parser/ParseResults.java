package parser;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ParseResults implements Serializable {

  private final String url;
  private final Set<String> urlsSet;

  ParseResults(String url) {
    this.url = url;
    urlsSet = Collections.synchronizedSet(new HashSet<>());
  }

  public String getUrl() {
    return url;
  }

  public Set<String> getUrlsSet() {
    return urlsSet;
  }

  public StringBuilder toStringBuilder(StringBuilder stringBuilder,
      int deepLevel) {
    if (deepLevel == 0) {
      stringBuilder.append(url);
      Parser.getAllParseResults().remove(url);
    }
    synchronized (urlsSet) {
      for (String urlKey : urlsSet) {
        stringBuilder.append("\r\n").append("\t".repeat(deepLevel + 1))
            .append(urlKey);
        ParseResults parseResults = Parser.getAllParseResults().remove(urlKey);
        if (parseResults != null) {
          parseResults.toStringBuilder(stringBuilder, deepLevel + 1);
        }
      }
    }
    return stringBuilder;
  }
}
