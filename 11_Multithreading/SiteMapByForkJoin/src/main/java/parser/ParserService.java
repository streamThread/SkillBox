package parser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ParserService {

  private static ParserService parserService;
  private ForkJoinPool forkJoinPool;
  private Parser parser;

  private ParserService() {
    forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
  }

  public static ParserService getInstance() {
    if (parserService == null) {
      parserService = new ParserService();
    }
    return parserService;
  }

  public void runParser(String inputUrl) {
    String checkedURL =
        List.of("https://" + inputUrl, "http://" + inputUrl, inputUrl).stream()
            .filter(this::pingUrl)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    parser = Parser.getInstance(checkedURL);
    if (forkJoinPool.isShutdown()) {
      forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
    }
    forkJoinPool.invoke(parser);
  }

  private boolean pingUrl(String url) {
    Document doc;
    try {
      doc = Jsoup.connect(url).get();
    } catch (IOException e) {
      return false;
    }
    return doc != null;
  }

  public void stopParser() {
    forkJoinPool.shutdownNow();
  }

  public String getResults() {
    return parser != null ? parser.getParseResults()
        .toStringBuilder(new StringBuilder(), 0).toString() : "";
  }

  public Integer getParsedLinksCount() {
    return ParseResults.getAlreadyParsed().size();
  }
}

