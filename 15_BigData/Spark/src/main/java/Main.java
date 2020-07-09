import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

public class Main {

  private static final Pattern SPACE = Pattern.compile("\\s+");

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: JavaWordCount <in_file> <out_file>");
      System.exit(1);
    }

    SparkSession spark = SparkSession
        .builder()
        .appName("JavaWordCount")
        .config("spark.master", "local")
        .getOrCreate();

    JavaRDD<String> lines = spark.read().textFile(args[0]).javaRDD();

    JavaRDD<String> words = lines
        .flatMap(s -> Arrays.asList(SPACE.split(s.replaceAll("[^А-яA-z\\s]",
            ""))).iterator());

    JavaPairRDD<String, Integer> ones = words.filter(s1 -> !s1.isEmpty())
        .mapToPair(s -> new Tuple2<>(s, 1));

    JavaPairRDD<String, Integer> counts = ones.reduceByKey(Integer::sum);

    JavaPairRDD<String, Integer> sortResult =
        counts.mapToPair(Tuple2::swap).sortByKey(false).mapToPair(Tuple2::swap);

    sortResult.saveAsTextFile(args[1]);

    spark.stop();
  }
}
