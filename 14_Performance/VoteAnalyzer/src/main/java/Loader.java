import util.XMLHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Loader {
    public static void main(String[] args) throws Exception {

        String fileName = "data-18M.xml";

        long start = System.currentTimeMillis();
        SAXParserFactory factory = SAXParserFactory.newDefaultInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(Loader.class.getClassLoader().getResourceAsStream(fileName), handler);
        handler.printResults();
        handler.printDublicatedVoters();
        System.out.println("Время работы программы: " + (System.currentTimeMillis() - start));
    }
}