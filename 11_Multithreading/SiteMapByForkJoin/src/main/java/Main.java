import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Main {
    public static void main(String[] args) {
        try {
            ForkJoinPool fjp = new ForkJoinPool();
            RecursiveTask<ParseResults> recursiveTask = new Parser("https://skillbox.ru");
            ParseResults parseResults = fjp.invoke(recursiveTask);
            printTextFile(parseResults, "11_Multithreading/SiteMapByForkJoin/src/main/resources/test.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printTextFile(ParseResults object, String filePath) throws IOException {
        StringBuilder stringBuilder = object.toStringBuilder(new StringBuilder(), 0);
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(filePath));
        try (bufferedWriter) {
            bufferedWriter.write(stringBuilder.toString());
        }
    }
}
