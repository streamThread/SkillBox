import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Log4j2
public class Parser extends RecursiveTask<Parser.ParseResults> {

    private static String mainUrl;
    private static Set<String> allreadyParsed = Collections.synchronizedSet(new HashSet<>());
    private ParseResults parseResults;

    Parser(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        parseResults = new ParseResults(url);
        if (mainUrl == null) {
            mainUrl = url;
            allreadyParsed.add(url);
        }
    }

    @Override
    protected ParseResults compute() {

        Document document;

        try {
            Thread.sleep(new Random().nextInt(100) + 100);
            document = Jsoup.connect(parseResults.URL).maxBodySize(0).get();
        } catch (IOException | InterruptedException e) {
            log.error(e);
            return parseResults;
        }

        for (Element e : document.select("a[href]")) {
            String absUrl = e.absUrl("href");
            if (absUrl.endsWith(".jpg") || absUrl.endsWith(".pdf") || absUrl.contains("#")
            || absUrl.endsWith(".png")) {
                continue;
            }
            if (!absUrl.endsWith("/")) {
                absUrl += "/";
            }
            if (!absUrl.equals(parseResults.URL) && absUrl.startsWith(mainUrl)) {
                parseResults.urlsSet.add(absUrl);
            }
        }

        ArrayList<Parser> parsers = new ArrayList<>();

        if (!parseResults.urlsSet.isEmpty()) {
            for (String str : parseResults.urlsSet) {
                if (allreadyParsed.add(str))
                    parsers.add(new Parser(str));
            }
        } else {
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
            parseResults.parseResultsMap.put(parser.parseResults.URL, joinParseResults);
        }
        return parseResults;
    }

    class ParseResults {

        private final String URL;
        private HashSet<String> urlsSet = new HashSet<>();
        private Map<String, ParseResults> parseResultsMap = new HashMap<>();

        ParseResults(String url) {
            URL = url;
        }

        public boolean isEmpty(){
            return parseResultsMap.isEmpty() && urlsSet.isEmpty();
        }

        public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
            if (urlsSet.isEmpty()) {
                return stringBuilder;
            }
            if (i == 0) {
                stringBuilder.append(URL);
            }
            Iterator<String> iterator = urlsSet.iterator();
            while (iterator.hasNext()) {
                String urlKey = iterator.next();
                stringBuilder.append("\r\n");
                for (int j = 0; j <= i; j++) {
                    stringBuilder.append("\t");
                }
                stringBuilder.append(urlKey);
                ParseResults parseResults = parseResultsMap.get(urlKey);
                if (parseResults != null) {
                    int count = i;
                    parseResults.toStringBuilder(stringBuilder, ++count);
                }
            }
            return stringBuilder;
        }
    }
}
