package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import parser.ParseResults;
import parser.Parser;

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
    parser = Parser.getInstance(inputUrl);
    if (forkJoinPool.isShutdown()) {
      forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
    }
    forkJoinPool.submit(parser);
  }

  public void stopParser() {
    forkJoinPool.shutdownNow();
  }

  public String getResults() {
    return parser.getParseResults().toStringBuilder(new StringBuilder(), 0)
        .toString();
  }

  public Integer getParsedLinksCount() {
    return ParseResults.getAlreadyParsed().size();
  }
}

