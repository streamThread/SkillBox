package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import parser.ParseResults;
import parser.Parser;

public class MainFormService {

  private static MainFormService mainFormService;
  private final ForkJoinPool forkJoinPool;
  private Parser parser;

  private MainFormService() {
    forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
  }

  public static MainFormService getInstance() {
    if (mainFormService == null) {
      mainFormService = new MainFormService();
    }
    return mainFormService;
  }

  public void runParser(String inputUrl) {
    parser = Parser.getInstance(inputUrl);
    forkJoinPool.submit(parser);
  }

  public void stopParser() {
    forkJoinPool.shutdownNow();
  }

  public String getResults() {
    return parser.getParseResults().toStringBuilder(new StringBuilder(), 0)
        .toString();
//    return new GsonBuilder().setPrettyPrinting().create()
//        .toJson(parser.getParseResults());
  }

  public Integer getParsedLinksCount() {
    return ParseResults.getAlreadyParsed().size();
  }
}

