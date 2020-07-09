import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

  static String enteredUrl;

  public static void main(String[] args) {
    while (true) {
      try {
        System.out.print("https://");
        Scanner scanner = new Scanner(System.in);
        enteredUrl = scanner.nextLine();
        if ("exit".equals(enteredUrl)) {
          break;
        }
        if (enteredUrl.matches("[А-я]+")) {
          System.out.println("Smeni raskladku :)");
          continue;
        }
        ForkJoinPool fjp = new ForkJoinPool();
        enteredUrl = "https://" + enteredUrl;

        RecursiveTask<Parser.ParseResults> recursiveTask = Parser
            .getInstance(enteredUrl);
        Parser.ParseResults parseResults = fjp.invoke(recursiveTask);
        if (parseResults.isEmpty()) {
          continue;
        }
        try {
          Files.createDirectory(Path.of("parse_results"));
        } catch (IOException ex) {
          log.debug(ex);
        }
        try {
          printTextFile(
              parseResults,
              String.format("parse_results/%s_map.txt",
                  enteredUrl.replaceAll("\\:?\\/", "_")));
        } catch (IOException ex) {
          log.info(ex);
        }
      } catch (RuntimeException e) {
        log.error(e);
      }
    }
  }

  private static void printTextFile(Parser.ParseResults object, String filePath)
      throws IOException {
    StringBuilder stringBuilder = object
        .toStringBuilder(new StringBuilder(), 0);
    BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(filePath));
    try (bufferedWriter) {
      bufferedWriter.write(stringBuilder.toString());
    }
  }
}
