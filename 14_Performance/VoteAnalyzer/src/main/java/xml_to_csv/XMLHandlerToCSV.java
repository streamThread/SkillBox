package xml_to_csv;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import model.Voter;
import model.WorkTime;
import org.xml.sax.Attributes;
import util.DBConnection;
import util.XMLHandler;

public class XMLHandlerToCSV extends XMLHandler {

  private final String PATH;
  private final CSVCreator csvCreator;

  public XMLHandlerToCSV(String filePath) {
    PATH = filePath;
    csvCreator = new CSVCreator(Path.of(filePath));
  }

  @Override
  public void startDocument() {
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) {
    if ("voter".equals(qName) && voter == null) {

      voter = new Voter(attributes.getValue("name"),
          LocalDate.parse(attributes.getValue("birthDay"), SIMPLE_DATE_FORMAT));

    } else if ("visit".equals(qName) && voter != null) {

      csvCreator.writeToCsv(voter);

      int station = Integer.parseInt(attributes.getValue("station"));
      LocalDateTime time = LocalDateTime
          .parse(attributes.getValue("time"), VISIT_DATE_FORMAT);

      voteStationWorkTimes
          .merge(station, new WorkTime(), (v1, v2) -> v1.addVisitTime(time));
    }
  }

  @Override
  public void endDocument() {
    dbConnection = new DBConnection();
    dbConnection.createTable();
    logger.info(INFO, "Конец документа");
    dbConnection.loadDataLocalInFile(PATH);
    dbConnection.close();
  }
}
