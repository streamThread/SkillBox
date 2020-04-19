import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@Data
public class ParseResults {

    String url;
    HashSet<String> urls = new HashSet<>();
    Map<String, ParseResults> parseResultsMap = new HashMap<>();

    ParseResults(String url) {
        this.url = url;
    }

    public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
        if (urls.isEmpty()) {
            return stringBuilder;
        }
        if (i == 0) {
            stringBuilder.append(url);
        }
        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext()) {
            String urlKey = iterator.next();
            stringBuilder.append("\n");
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
