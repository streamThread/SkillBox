import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Main {

    private static final String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    private static final String JSON_FILE_OUTPATH = "data/test.json";
    private static final String URL_REGEX = "^https?:\\/\\/.+\\..+\\/?$";

    public static void main(String[] args) {
        try {
            Document doc = connectToUrl(URL);
            Parser parser = new Parser();
            JsonObject all = new JsonObject();
            all.add("stations", parser.generateStationsJsonObj(doc));
            all.add("lines", parser.generateLinesJsonObj(doc));

//            printJson(all);
            parser.changeStationsInConnections();
            all.add("connections", parser.getConnection());
            printJson(all);
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

    private static void printJson(JsonObject object) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer out = new BufferedWriter(new FileWriter(JSON_FILE_OUTPATH));
        gson.toJson(object, out);
        out.flush();
        out.close();
    }
}

