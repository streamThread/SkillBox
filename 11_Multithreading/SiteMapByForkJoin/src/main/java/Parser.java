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
            parseResults.setEmpty(true);
            return parseResults;
        }

        for (Element e : document.select("a[href]")) {
            String absUrl = e.absUrl("href");
            if (!absUrl.equals(URL) && !absUrl.equals(URL + "/") && absUrl.startsWith(mainUrl)) {
                parseResults.getUrls().add(absUrl);
            }
        }

        ArrayList<Parser> parsers = new ArrayList<>();

        if (!parseResults.getUrls().isEmpty()) {
            for (String str : parseResults.getUrls()) {
                if (allreadyParsed.add(str) && !str.contains("#"))
                    parsers.add(new Parser(str));
            }
        } else {
            parseResults.setEmpty(true);
            return parseResults;
        }

        for (Parser parser : parsers) {
            parser.fork();
        }

        for (Parser parser : parsers) {
            ParseResults joinParseResults = parser.join();
            if (joinParseResults.isEmpty()) {
                continue;
            }
            parseResults.getParseResultsMap().put(parser.URL, joinParseResults);
        }
        return parseResults;
    }
}
