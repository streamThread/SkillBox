import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@Data
class ParseResults {

    @Getter(AccessLevel.NONE)
    private final String URL;
    @Setter(AccessLevel.NONE)
    private HashSet<String> urls = new HashSet<>();
    @Setter(AccessLevel.NONE)
    private Map<String, ParseResults> parseResultsMap = new HashMap<>();
    private boolean isEmpty;

    ParseResults(String url) {
        URL = url;
    }

    public StringBuilder toStringBuilder(StringBuilder stringBuilder, int i) {
        if (urls.isEmpty()) {
            return stringBuilder;
        }
        if (i == 0) {
            stringBuilder.append(URL);
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
