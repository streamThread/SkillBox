import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.gson.JsonParser.parseString;

public class Main {

    private static final String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    private static final String JSON_FILE_OUTPATH = "data" + File.separator + "test.json";
    private static final String URL_REGEX = "^https?:\\/\\/.+\\..+\\/?$";

    public static void main(String[] args) {
        try {
            Document doc = connectToUrl(URL);
            Parser parser = new Parser();
            JsonObject result = parser.parseMetro(doc);
            printJson(result, JSON_FILE_OUTPATH);
            JsonObject data = readJson(JSON_FILE_OUTPATH);
            printStationsCount(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document connectToUrl(String url) throws IOException {
        if (!url.matches(URL_REGEX)) {
            throw new IllegalArgumentException("Wrong url argument");
        }
        return Jsoup.connect(url).get();
    }

    private static void printJson(JsonObject object, String filePath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer out = new BufferedWriter(new FileWriter(filePath));
        gson.toJson(object, out);
        out.flush();
        out.close();
    }

    private static JsonObject readJson(String filePath) throws IOException {
        String text = Files.readString(Paths.get(filePath));
        return (JsonObject) parseString(text);
    }

    private static void printStationsCount(JsonObject object) {
        for (JsonElement jsonElement : object.getAsJsonArray("lines")) {
            JsonObject line = (JsonObject) jsonElement;
            String lineName = line.get("name").getAsString();
            String lineNum = line.get("number").getAsString();
            int stationsCount = object.getAsJsonObject("stations").getAsJsonArray(lineNum).size();
            int stringLength = 33 - lineName.length();
            String form = "%" + stringLength + "s";
            System.out.printf("Количество действующих станций на линии " + form + " %s : %d\n", "", lineName, stationsCount);
        }
    }
}

