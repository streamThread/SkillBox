import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class Writer implements Runnable {

    private static final AtomicInteger fileNum = new AtomicInteger(1);
    private final Path directory;
    Future<StringBuilder> nums;
    int procNum;

    public Writer(Future<StringBuilder> nums, Path directory, int procNum) {
        this.nums = nums;
        this.directory = directory;
        this.procNum = procNum;
    }

    @Override
    public void run() {

        fileNum.compareAndSet(procNum + 1, 1);

        Path filePath = directory.resolve(String.format("result%d.txt", fileNum.getAndIncrement()));

        try {
            Files.write(filePath, nums.get().toString().getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException | InterruptedException | ExecutionException exception) {
            log.error(exception);
        }
    }
}
