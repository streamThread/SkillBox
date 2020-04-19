import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Log4j2
public class Parser extends RecursiveTask<ParseResults> {

    private static String mainUrl;
    private static Set<String> allreadyParsed = Collections.synchronizedSet(new HashSet<>());
    private final String URL;
    private ParseResults parseResults;

    Parser(String url) {
        URL = url;
        parseResults = new ParseResults(url);
        if (mainUrl == null) {
            mainUrl = url;
        }
    }

    @Override
    protected ParseResults compute() {

        Document document;

        try {
            Thread.sleep(new Random().nextInt(100) + 100);
            document = Jsoup.connect(URL).maxBodySize(0).get();
        } catch (IOException | InterruptedException e) {
            log.error(e);
            return null;
        }

        for (Element e : document.select("[href]")) {
            String absUrl = e.absUrl("href");
            if (!absUrl.equals(URL) && !absUrl.equals(URL + "/") && absUrl.startsWith(mainUrl) && allreadyParsed.add(absUrl)) {
                parseResults.urls.add(absUrl);
            }
        }

        ArrayList<Parser> parsers = new ArrayList<>();

        if (!parseResults.urls.isEmpty()) {
            for (String str : parseResults.urls) {
                parsers.add(new Parser(str));
            }
        } else return null;

        for (Parser parser : parsers) {
            parser.fork();
        }

        for (Parser parser : parsers) {
            ParseResults joinParseResults = parser.join();
            if (joinParseResults == null) {
                continue;
            }
            parseResults.parseResultsMap.put(parser.URL, joinParseResults);
        }
        return parseResults.urls.isEmpty() ? null : parseResults;
    }
}
