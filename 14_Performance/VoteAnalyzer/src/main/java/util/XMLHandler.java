package util;

import model.Voter;
import model.WorkTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class XMLHandler extends DefaultHandler {

    protected static final DateTimeFormatter VISIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    protected static final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter SIMPLE_DATE_FORMAT_TO_DB = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    protected static final Marker INFO = MarkerManager.getMarker("INFO");
    protected static final Logger logger = LogManager.getRootLogger();
    protected final HashMap<Integer, WorkTime> voteStationWorkTimes = new HashMap<>();
    protected DBConnection dbConnection;
    protected Voter voter;

    @Override
    public void startDocument() {
        dbConnection = new DBConnection();
        dbConnection.createTable();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if ("voter".equals(qName) && voter == null) {

            voter = new Voter(attributes.getValue("name"),
                    LocalDate.parse(attributes.getValue("birthDay"), SIMPLE_DATE_FORMAT));

        } else if ("visit".equals(qName) && voter != null) {

            dbConnection.countVoter(voter.getName(), SIMPLE_DATE_FORMAT_TO_DB.format(voter.getBirthDay()));

            int station = Integer.parseInt(attributes.getValue("station"));
            LocalDateTime time = LocalDateTime.parse(attributes.getValue("time"), VISIT_DATE_FORMAT);

            voteStationWorkTimes.merge(station, new WorkTime(), (v1, v2) -> v1.addVisitTime(time));
        }
    }

    @Override
    public void endDocument() {
        dbConnection.executeMultiInsert();
        dbConnection.setLinesCount(0);
        logger.info(INFO, "Конец документа");
        dbConnection.close();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("voter".equals(qName)) {
            voter = null;
        }
    }

    public void printDublicatedVoters() {
        try (DBConnection connection = new DBConnection()) {
            logger.info(INFO, connection.getStringOfVoterCounts());
        } catch (SQLException ex) {
            logger.error(ex);
        }
    }

    public void printResults() {
        logger.info(INFO, "Voting station work times: ");
        for (Map.Entry<Integer, WorkTime> entry : voteStationWorkTimes.entrySet()) {
            WorkTime workTime = entry.getValue();
            if (logger.isInfoEnabled()) {
                logger.info(INFO, String.format("\t%d - %s", entry.getKey(), workTime));
            }
        }
    }
}
