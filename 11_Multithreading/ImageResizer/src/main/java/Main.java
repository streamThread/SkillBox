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

    static final String SRC_FOLDER = "src/main/resources/from";
    static final String DSC_FOLDER = "src/main/resources/to";

    public static void main(String[] args) {
        try {
            File srcDir = new File(SRC_FOLDER);

            long start = System.currentTimeMillis();

            Deque<File> deque = new ArrayDeque<>();

            Collections.addAll(deque, Objects.requireNonNull(srcDir.listFiles()));

            int procNum = Runtime.getRuntime().availableProcessors();

            if (!Files.exists(Path.of(DSC_FOLDER))) Files.createDirectory(Path.of(DSC_FOLDER));

            Stream.generate(() -> new ImageResizer(deque, DSC_FOLDER, start, 300))
                    .limit(procNum)
                    .parallel()
                    .forEach(Thread::start);

        } catch (IOException | RuntimeException e) {
            log.error(e);
        }
    }
}
