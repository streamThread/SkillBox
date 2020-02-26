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

    public JsonArray getConnection() {
        return connection;
    }

    public JsonArray generateLinesJsonObj(Document doc) {
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

    public JsonObject generateStationsJsonObj(Document document) throws IllegalArgumentException {
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
            JsonObject temp = new JsonObject();
            JsonArray arrTemp = new JsonArray();
            temp.addProperty("line", lineNumber);
            temp.addProperty("station", lineName);
            arrTemp.add(temp);
            for (int i = 0; i < lineNums.length && i < lineNames.length; i++) {
                temp = new JsonObject();
                temp.addProperty("line", lineNums[i].text());
                String stationName = lineNames[i].attr("title");
                temp.addProperty("station", stationName);
                arrTemp.add(temp);
            }
            connection.add(arrTemp);
        }
    }

    void changeStationsInConnections() {
        if (stationsWasGenerated) {
            for (int j = 0; j < connection.size(); j++) {
                JsonArray arr = connection.get(j).getAsJsonArray();
                for (int i = 1; i < arr.size(); i++) {
                    String lineNum = arr.get(i).getAsJsonObject().get("line").getAsString();
                    if (!stationsJsonObj.keySet().contains(lineNum)) {
                        arr.remove(i);
                        if (connection.get(j).getAsJsonArray().size() <= 1) {
                            connection.remove(j);
                            j--;
                        }
                        continue;
                    }
                    JsonArray jsonArrayTemp = stationsJsonObj.get(lineNum).getAsJsonArray();
                    for (int k = 0; k < jsonArrayTemp.size(); k++) {
                        String lineName = jsonArrayTemp.get(k).getAsString();
                        Pattern pattern = Pattern.compile(lineName);
                        Matcher matcher = pattern.matcher(arr.get(i).getAsJsonObject().get("station").getAsString());
                        if (matcher.find()) {
                            arr.get(i).getAsJsonObject().addProperty("station", lineName);
                            break;
                        }
                        if (jsonArrayTemp.size() - 1 == k) {
                            arr.remove(i);
                            i--;
                            if (connection.get(j).getAsJsonArray().size() <= 1) {
                                connection.remove(j);
                                j--;
                            }
                        }
                    }
                }
            }
        }
    }

    void dfgsfg(JsonArray arr){

    }

}


