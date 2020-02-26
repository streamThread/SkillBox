import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    //Pointer to the first string of table with the name 'Станции Московского метрополитена':
    private static final String cSS_QUERY_TO_BODY_TABLE_METRO = "#mw-content-text > div > table:nth-child(7) > tbody > tr:nth-child(1)";
    //Pointer to the first string of table with the name 'Платформы Московского центрального кольца':
    private static final String cSS_QUERY_TO_BODY_TABLE_CIRCLE = "#mw-content-text > div > table:nth-child(11) > tbody > tr:nth-child(2)";
    //Pointer to the first string of table with the name 'Станции Московского монорельса':
    private static final String cSS_QUERY_TO_BODY_TABLE_MONO = "#mw-content-text > div > table:nth-child(9) > tbody > tr:nth-child(2)";
    //Pointer to the strings of table with the name 'Линии Московского метрополитена':
    private static final String cSS_QUERY_TO_TABLE_ELEMENTS_LINES = "tbody > tr:nth-child(2) > td > div > table > tbody > tr > td > div > dl > dd";
    private JsonArray lines = new JsonArray();
    private JsonObject stationsJsonObj = new JsonObject();
    private JsonArray connection = new JsonArray();
    private boolean stationsWasGenerated;
    private boolean linesWasGenerated;

    public JsonObject parseMetro(Document doc) {
        JsonObject object = new JsonObject();
        object.add("stations", generateStationsJsonObj(doc));
        object.add("connections", chekAndChangeStationsInConnections());
        object.add("lines", generateLinesJsonObj(doc));
        return object;
    }


    private JsonArray generateLinesJsonObj(Document doc) {
        if (linesWasGenerated) lines = new JsonArray();
        Elements el = doc.select(cSS_QUERY_TO_TABLE_ELEMENTS_LINES);
        Iterator<Element> it = el.iterator();
        while (it.hasNext()) {
            Element temp = it.next();
            String numberLine = temp.select("dd>span:first-child").text();
            String lineName = temp.select("dd>a:nth-child(3)").text();
            JsonObject tempo = new JsonObject();
            tempo.addProperty("number", numberLine);
            tempo.addProperty("name", lineName);
            lines.add(tempo);
        }
        linesWasGenerated = true;
        return lines;
    }

    private JsonObject generateStationsJsonObj(Document document) throws IllegalArgumentException {
        Elements tableMetro = document.select(cSS_QUERY_TO_BODY_TABLE_METRO).nextAll("tr").not("tr.shadow");
        Elements tableCircle = document.select(cSS_QUERY_TO_BODY_TABLE_CIRCLE).nextAll("tr");
        Elements tableMono = document.select(cSS_QUERY_TO_BODY_TABLE_MONO).nextAll("tr");

        if (stationsWasGenerated) stationsJsonObj = new JsonObject();
        parseStationsFromElements(tableMetro);
        parseStationsFromElements(tableCircle);
        parseStationsFromElements(tableMono);

        stationsWasGenerated = true;
        return stationsJsonObj;
    }

    private void parseStationsFromElements(Elements elements) {
        Iterator<Element> iter = elements.iterator();
        while (iter.hasNext()) {
            Element elTemp = iter.next();
            String station = elTemp.select("tr > td:nth-child(2) > span > a").text();
            if (station.isBlank()) station = elTemp.selectFirst("tr > td:nth-child(2) > a").text();
            String lineNumber = elTemp.selectFirst("tr > td:nth-child(1) > span:first-child").text();
            String secondLineNumber = elTemp.select("tr > td:nth-child(1) > span:nth-child(4)").text();
            addStationToJsonObj(station, lineNumber, stationsJsonObj);
            if (!secondLineNumber.isBlank()) {
                addStationToJsonObj(station, secondLineNumber, stationsJsonObj);
            }
            parseConnections(station, lineNumber, elTemp);
        }
    }

    private boolean addStationToJsonObj(String station, String lineNumber, JsonObject obj) {
        JsonArray stationsArr = new JsonArray();
        if (obj.keySet().contains(lineNumber)) {
            obj.getAsJsonArray(lineNumber).add(station);
            return true;
        }
        stationsArr.add(station);
        obj.add(lineNumber, stationsArr);
        return true;
    }

    private void parseConnections(String lineName, String lineNumber, Element el) {
        Element[] lineNums = el.select("tr>td:nth-child(4)").not("td[data-sort-value=Infinity]")
                .select("span:nth-child(odd)").toArray(new Element[0]);
        Element[] lineNames = el.select("tr>td:nth-child(4)").not("td[data-sort-value=Infinity]")
                .select("span:nth-child(even)").toArray(new Element[0]);
        if (lineNums.length > 0 && lineNames.length > 0) {
            JsonArray arrTemp = new JsonArray();
            addStationToTempJarr(lineNumber, lineName, arrTemp);
            for (int i = 0; i < lineNums.length && i < lineNames.length; i++) {
                String lineNum = lineNums[i].text();
                String stationName = lineNames[i].attr("title");
                addStationToTempJarr(lineNum, stationName, arrTemp);
            }
            connection.add(arrTemp);
        }
    }

    private JsonArray addStationToTempJarr(String lineNumber, String lineName, JsonArray arrTemp) {
        JsonObject temp = new JsonObject();
        temp.addProperty("line", lineNumber);
        temp.addProperty("station", lineName);
        arrTemp.add(temp);
        return arrTemp;
    }

    private JsonArray chekAndChangeStationsInConnections() throws IllegalStateException {
        if (!stationsWasGenerated) {
            throw new IllegalStateException("Stations wasn't generated");
        }
        for (int j = 0; j < connection.size(); j++) {
            JsonArray arr = connection.get(j).getAsJsonArray();
            if (deleteInvalidObjects(arr)) {
                j--;
                continue;
            }
            for (int i = 1; i < arr.size(); i++) {
                JsonObject objectTemp = arr.get(i).getAsJsonObject();
                String lineNum = arr.get(i).getAsJsonObject().get("line").getAsString();
                String connectionName = arr.get(i).getAsJsonObject().get("station").getAsString();
                if (!changeStationName(lineNum, connectionName, objectTemp)) {
                    arr.remove(i);
                    i--;
                }
                if (connection.get(j).getAsJsonArray().size() <= 1) {
                    connection.remove(j);
                    j--;
                    break;
                }
            }
        }
        return connection;
    }

    private boolean deleteInvalidObjects(JsonArray arr) {
        for (int i = 1; i < arr.size(); i++) {
            String lineNum = arr.get(i).getAsJsonObject().get("line").getAsString();
            if (!stationsJsonObj.keySet().contains(lineNum)) {
                arr.remove(i);
                i--;
            }
            if (arr.size() <= 1) {
                connection.remove(arr);
                return true;
            }
        }
        return false;
    }

    private boolean changeStationName(String lineNum, String connectionName, JsonObject objTemp) {
        JsonArray jsonArrayTemp = stationsJsonObj.get(lineNum).getAsJsonArray();
        for (int k = 0; k < jsonArrayTemp.size(); k++) {
            String lineName = jsonArrayTemp.get(k).getAsString();
            Pattern pattern = Pattern.compile(lineName);
            Matcher matcher = pattern.matcher(connectionName);
            if (matcher.find()) {
                objTemp.addProperty("station", lineName);
                return true;
            }
        }
        return false;
    }
}




