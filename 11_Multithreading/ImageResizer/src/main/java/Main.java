import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Objects;
import java.util.stream.Stream;

@Log4j2
public class Main {
    public static void main(String[] args) {
        try {
            String srcFolder = "src/main/resources/from";
            String dstFolder = "src/main/resources/to";

            File srcDir = new File(srcFolder);

            long start = System.currentTimeMillis();

            Deque<File> deque = new ArrayDeque<>();

            Collections.addAll(deque, Objects.requireNonNull(srcDir.listFiles()));

            int procNum = Runtime.getRuntime().availableProcessors();

            if (!Files.exists(Path.of(dstFolder))) Files.createDirectory(Path.of(dstFolder));

            Stream.generate(() -> new ImageResizer(deque, dstFolder, start, 300))
                    .limit(procNum)
                    .parallel()
                    .forEach(Thread::start);

        } catch (IOException | RuntimeException e) {
            log.error(e);
        }
    }
}
