package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TextFileWriter {

  private static final ReentrantLock lock = new ReentrantLock();
  private static final Path directoryPath = Path.of("parse_results");
  private static final String FILE_NAME = "parse_results/%s_map.txt";

  public void saveResults(String inputUrl, String results) {
    lock.lock();
    try {
      Files.createDirectory(directoryPath);
    } catch (IOException e) {
      log.debug("", e);
    }
    printTextFile(String.format(FILE_NAME, inputUrl.replaceAll("\\:?\\/",
        "_")), results);
    lock.unlock();
  }

  private void printTextFile(String filePath, String results) {
    try (BufferedWriter bufferedWriter = Files
        .newBufferedWriter(Path.of(filePath))) {
      bufferedWriter.write(results);
    } catch (IOException ioException) {
      log.error("", ioException);
    }
  }
}
