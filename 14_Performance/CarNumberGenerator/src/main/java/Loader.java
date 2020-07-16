import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loader {

  private static final Logger log = LogManager.getFormatterLogger(Loader.class);
  private static final Path DEST_DIR = Path.of("result");

  public static void main(String[] args) throws Exception {

    if (Files.exists(DEST_DIR, LinkOption.NOFOLLOW_LINKS)) {
      Files.walk(DEST_DIR)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
    Files.createDirectory(DEST_DIR);

    long start = System.currentTimeMillis();
    int procNum = Runtime.getRuntime().availableProcessors();
    ExecutorService service = Executors.newFixedThreadPool(procNum);

    for (int regionCode = 1; regionCode < 100; regionCode++) {
      service.submit(new Writer(
          service.invokeAny(Collections.singleton(new Generator(regionCode))),
          DEST_DIR,
          procNum));
    }

    service.shutdown();
    service.awaitTermination(1L, TimeUnit.DAYS);

    log.info("%d ms", () -> System.currentTimeMillis() - start);
  }
}
