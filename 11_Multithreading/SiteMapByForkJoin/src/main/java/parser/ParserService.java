package parser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Log4j2
public class ParserService {

  private static final int CACHE_SIZE = Integer.MAX_VALUE / 2;
  private ForkJoinPool forkJoinPool;
  private Parser parser;

  public ParserService() {
    forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
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
    while (true) {
      if (forkJoinPool.isTerminated()) {
        break;
      }
    }
  }

  public void resumeParser() {
    forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
    Parser.getAllParseResults().values().parallelStream()
        .filter(parseResults -> parseResults.getUrlsSet().isEmpty())
        .map(ParseResults::getUrl)
        .map(url -> new Parser(url))
        .forEach(newParser -> forkJoinPool.submit(newParser));
  }

  public String getFirstFewFoundLinks(int countOfFew) {
    synchronized (Parser.getFoundUrlsCache()) {
      int urlsCountInCache = Parser.getFoundUrlsCache().size();
      String result = String.join("\r\n", Parser.getFoundUrlsCache()
          //берем на выдачу последние countOfFew записей
          .subList(
              urlsCountInCache > countOfFew ? urlsCountInCache - countOfFew - 1
                  : 0,
              urlsCountInCache == 0 ? urlsCountInCache : urlsCountInCache - 1));
      if (urlsCountInCache > CACHE_SIZE) {
        //чистим кэш
        Parser.getFoundUrlsCache()
            .subList(0, urlsCountInCache - (countOfFew * 2)).clear();
      }
      return result;
    }
  }

  public long getFoundLinksCount() {
    return Parser.getAllParseResults().values().stream()
        .mapToLong(parseResults -> parseResults.getUrlsSet().size())
        .sum() + 1;
  }

  public String getResultString() {
    return parser != null ? parser.getParseResults()
        .toStringBuilder(new StringBuilder(), 0).toString() : "";
  }
}



