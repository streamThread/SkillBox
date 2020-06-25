import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import util.XMLHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Loader {

    private static final Marker INFO = MarkerManager.getMarker("INFO");
    private static final Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) throws Exception {

        String fileName = "data-1572M.xml";

        long start = System.currentTimeMillis();
        SAXParserFactory factory = SAXParserFactory.newDefaultInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(Loader.class.getClassLoader().getResourceAsStream(fileName), handler);
        handler.printResults();
        handler.printDublicatedVoters();

        if (logger.isInfoEnabled()) {
            logger.info(INFO, String.format("Время работы программы: %d", System.currentTimeMillis() - start));
        }
    }
}