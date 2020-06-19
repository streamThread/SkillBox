import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class XMLHandler extends DefaultHandler {

    private static final SimpleDateFormat VISIT_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    private static final HashMap<Integer, WorkTime> voteStationWorkTimes = new HashMap<>();
    private final DBConnection dbConnection = new DBConnection();
    private Voter voter;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if ("voter".equals(qName) && voter == null) {

                voter = new Voter(attributes.getValue("name"),
                        SIMPLE_DATE_FORMAT.parse(attributes.getValue("birthDay")));

            } else if ("visit".equals(qName) && voter != null) {

                dbConnection.countVoter(voter.getName(), SIMPLE_DATE_FORMAT.format(voter.getBirthDay()));

                int station = Integer.parseInt(attributes.getValue("station"));
                Date time = VISIT_DATE_FORMAT.parse(attributes.getValue("time"));

                voteStationWorkTimes.merge(station, new WorkTime(), (v1, v2) -> v1.addVisitTime(time.getTime()));
            }
        } catch (ParseException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            dbConnection.executeMultiInsert();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("voter".equals(qName)) {
            voter = null;
        }
    }

    public void printDublicatedVoters() {
        try {
            System.out.println(dbConnection.getStringOfVoterCounts());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void printResults() {
        System.out.println("Voting station work times: ");
        for (Integer votingStation : voteStationWorkTimes.keySet()) {
            WorkTime workTime = voteStationWorkTimes.get(votingStation);
            System.out.println("\t" + votingStation + " - " + workTime);
        }
    }
}
