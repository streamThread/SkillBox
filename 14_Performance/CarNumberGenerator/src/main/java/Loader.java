import auxiliary.MyFileVisitor;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class Loader {

    private static final Path DEST_DIR = Path.of("result");

    public static void main(String[] args) throws Exception {

        if (Files.exists(DEST_DIR)) {
            Files.walkFileTree(DEST_DIR, new MyFileVisitor()); //recursive delete
        }
        Files.createDirectory(DEST_DIR);

        long start = System.currentTimeMillis();
        int procNum = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(procNum);

        for (int regionCode = 1; regionCode < 100; regionCode++) {
            service.submit(new Writer(
                    service.invokeAny(Collections.singleton(new Generator())),
                    DEST_DIR,
                    procNum));
        }

        service.shutdown();
        service.awaitTermination(1L, TimeUnit.DAYS);

        log.info((System.currentTimeMillis() - start) + " ms");
    }
}
