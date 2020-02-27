import jsonclasses.Line;
import jsonclasses.Result;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {

    //Pointer to the first string of table with the name 'Станции Московского метрополитена':
    private static final String cSS_QUERY_TO_BODY_TABLE_METRO = "#mw-content-text > div > table:nth-child(7) > tbody > tr:nth-child(1)";
    //Pointer to the first string of table with the name 'Платформы Московского центрального кольца':
    private static final String cSS_QUERY_TO_BODY_TABLE_CIRCLE = "#mw-content-text > div > table:nth-child(11) > tbody > tr:nth-child(2)";
    //Pointer to the first string of table with the name 'Станции Московского монорельса':
    private static final String cSS_QUERY_TO_BODY_TABLE_MONO = "#mw-content-text > div > table:nth-child(9) > tbody > tr:nth-child(2)";
    //Pointer to the strings of table with the name 'Линии Московского метрополитена':
    private static final String cSS_QUERY_TO_TABLE_ELEMENTS_LINES = "tbody > tr:nth-child(2) > td > div > table > tbody > tr > td > div > dl > dd";
    Result result = new Result();
    private boolean parsingComplete;
    private boolean stationsWasGenerated;


    public Result parseMetro(Document doc) {
        if (parsingComplete) result = new Result();
        generateStations(doc);
        generateLines(doc);
        chekAndChangeStationsInConnections();
        parsingComplete = true;
        return result;
    }

    private void generateLines(Document doc) {
        Elements el = doc.select(cSS_QUERY_TO_TABLE_ELEMENTS_LINES);
        Iterator<Element> it = el.iterator();
        while (it.hasNext()) {
            Element temp = it.next();
            String numberLine = temp.select("dd>span:first-child").text();
            String lineName = temp.select("dd>a:nth-child(3)").text();
            Line line = new Line(numberLine, lineName);
            result.getLines().add(line);
        }
    }

    private void generateStations(Document document) throws IllegalArgumentException {
        Elements tableMetro = document.select(cSS_QUERY_TO_BODY_TABLE_METRO).nextAll("tr").not("tr.shadow");
        Elements tableCircle = document.select(cSS_QUERY_TO_BODY_TABLE_CIRCLE).nextAll("tr");
        Elements tableMono = document.select(cSS_QUERY_TO_BODY_TABLE_MONO).nextAll("tr");

        parseStationsFromElements(tableMetro);
        parseStationsFromElements(tableCircle);
        parseStationsFromElements(tableMono);
        stationsWasGenerated = true;
    }

    private void parseStationsFromElements(Elements elements) {
        Iterator<Element> iter = elements.iterator();
        while (iter.hasNext()) {
            Element elTemp = iter.next();
            String station = elTemp.select("tr > td:nth-child(2) > span > a").text();
            if (station.isBlank()) station = elTemp.selectFirst("tr > td:nth-child(2) > a").text();
            String lineNumber = elTemp.selectFirst("tr > td:nth-child(1) > span:first-child").text();
            String secondLineNumber = elTemp.select("tr > td:nth-child(1) > span:nth-child(4)").text();
            addStationToResult(station, lineNumber);
            if (!secondLineNumber.isBlank()) {
                addStationToResult(station, secondLineNumber);
            }
            parseConnections(station, lineNumber, elTemp);
        }
    }

    private boolean addStationToResult(String station, String lineNumber) {
        if (result.getStations().containsKey(lineNumber)) {
            result.getStations().get(lineNumber).add(station);
            return true;
        }
        String[] stationArr = {station};
        result.getStations().put(lineNumber, new ArrayList<>(Arrays.asList(stationArr)));
        return true;
    }

    private void parseConnections(String lineName, String lineNumber, Element el) {
        Element[] lineNums = el.select("tr>td:nth-child(4)").not("td[data-sort-value=Infinity]")
                .select("span:nth-child(odd)").toArray(new Element[0]);
        Element[] lineNames = el.select("tr>td:nth-child(4)").not("td[data-sort-value=Infinity]")
                .select("span:nth-child(even)").toArray(new Element[0]);
        if (lineNums.length > 0 && lineNames.length > 0) {
            List<Line> connectionsList = new ArrayList<>();
            connectionsList.add(new Line(lineNumber, lineName));
            for (int i = 0; i < lineNums.length && i < lineNames.length; i++) {
                String lineNum = lineNums[i].text();
                String stationName = lineNames[i].attr("title");
                connectionsList.add(new Line(lineNum, stationName));
            }
            result.getConnections().add(connectionsList);
        }
    }

    private void chekAndChangeStationsInConnections() throws IllegalStateException {
        if (!stationsWasGenerated) {
            throw new IllegalStateException("Stations wasn't generated");
        }
        result.setConnections(result.getConnections().stream()
                .filter(this::excludeInvalidObjects)
                .map(a -> a.stream()
                        .filter(b -> changeStationName(b.getNumber(), b.getName(), b))
                        .sorted()
                        .collect(Collectors.toList()))
                .collect(Collectors.toSet()));
    }

    private boolean excludeInvalidObjects(List<Line> connections) {
        long elementsCount = connections.stream()
                .filter(conLine -> result.getStations().containsKey(conLine.getNumber()))
                .count();
        return elementsCount > 1;
    }

    private boolean changeStationName(String lineNum, String connectionName, Line line) {
        for (String lineName : result.getStations().get(lineNum)) {
            Matcher matcher = Pattern.compile(lineName).matcher(connectionName);
            if (matcher.find()) {
                line.setName(lineName);
                return true;
            }
        }
        return false;
    }
}




