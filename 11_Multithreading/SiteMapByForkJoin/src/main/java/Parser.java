import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class Parser extends RecursiveTask<StringBuffer> {

    private static Set<String> allreadyParsed = Collections.synchronizedSet(new HashSet<>());
    private final String URL;
    private Set<String> parsedUrls = new HashSet<>();
    private StringBuffer url = new StringBuffer();

    Parser(String url) {
        URL = url;
    }

    @Override
    protected StringBuffer compute() {

        try {
            Thread.sleep(new Random().nextInt(100) + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        Document document;
        try {
            document = Jsoup.connect(URL).maxBodySize(0).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        for (Element e : document.select("[href]")) {
            String absUrl = e.absUrl("href");
            if (!absUrl.equals(URL) && absUrl.startsWith("https://lenta.ru") && allreadyParsed.add(absUrl)) {
                parsedUrls.add(absUrl);
                url.append(URL+"\n\t"+absUrl);
            }
        }

        ArrayList<Parser> parsers = new ArrayList();

        if (!parsedUrls.isEmpty()) {
            for (String str : parsedUrls) {
                parsers.add(new Parser(str));
            }
        } else return null;

        for (Parser parser : parsers) {
            parser.fork();
        }

        for (Parser parser : parsers) {
            StringBuffer sb;
            if ((sb = parser.join()) != null) {
                    url.append("\n\t" + sb);
                }
            }
        System.out.println(url);
        return url;
    }
}
