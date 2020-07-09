import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import xml_to_csv.XMLHandlerToCSV;

public class Loader {

  private static final Marker INFO = MarkerManager.getMarker("INFO");
  private static final Logger logger = LogManager.getRootLogger();

  public static void main(String[] args) throws Exception {

    String fileName = "data-18M.xml";

    long start = System.currentTimeMillis();
    SAXParserFactory factory = SAXParserFactory.newDefaultInstance();
    SAXParser parser = factory.newSAXParser();
    XMLHandlerToCSV handler = new XMLHandlerToCSV("csv/csv_to_db.csv");
    parser.parse(Loader.class.getClassLoader().getResourceAsStream(fileName),
        handler);
    handler.printResults();
    handler.printDublicatedVoters();

    if (logger.isInfoEnabled()) {
      logger.info(INFO, String.format("Время работы программы: %d",
          System.currentTimeMillis() - start));
    }
  }
}