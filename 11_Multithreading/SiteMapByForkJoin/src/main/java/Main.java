import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Main {
    public static void main(String[] args) {
        ForkJoinPool fjp = new ForkJoinPool();
        RecursiveTask<StringBuffer> recursiveTask = new Parser("https://lenta.ru");
        System.out.println( fjp.invoke(recursiveTask));
    }
}
