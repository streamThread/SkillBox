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
            RecursiveTask<Parser.ParseResults> recursiveTask = new Parser("https://skillbox.ru");
            Parser.ParseResults parseResults = fjp.invoke(recursiveTask);
            printTextFile(parseResults, "11_Multithreading/SiteMapByForkJoin/src/main/resources/map.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printTextFile(Parser.ParseResults object, String filePath) throws IOException {
        StringBuilder stringBuilder = object.toStringBuilder(new StringBuilder(), 0);
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of(filePath));
        try (bufferedWriter) {
            bufferedWriter.write(stringBuilder.toString());
        }
    }
}
