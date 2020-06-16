import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Generator implements Callable<StringBuilder> {

    private static final AtomicInteger regionCode = new AtomicInteger(1);
    private final char[] LETTERS = {'У', 'К', 'Е', 'Н', 'Х', 'В', 'А', 'Р', 'О', 'С', 'М', 'Т'};

    @Override
    public StringBuilder call() {
        StringBuilder stringBuilder = new StringBuilder(17262720);
        String regionCodeWithLeadingZeros = padNumber(regionCode.getAndIncrement(), 2);
        for (int number = 1; number < 1000; number++) {
            String mainNumberWithLeadingZeros = padNumber(number, 3);
            for (char firstLetter : LETTERS) {
                for (char secondLetter : LETTERS) {
                    for (char thirdLetter : LETTERS) {
                        stringBuilder.append(firstLetter)
                                .append(mainNumberWithLeadingZeros)
                                .append(secondLetter)
                                .append(thirdLetter)
                                .append(regionCodeWithLeadingZeros)
                                .append("\r\n");
                    }
                }
            }
        }
        return stringBuilder;
    }

    private String padNumber(int number, int numberLength) {
        String numberStr = Integer.toString(number);
        int padSize = numberLength - numberStr.length();
        if (padSize > 0) {
            return "0".repeat(padSize) +
                    number;
        }
        return numberStr;
    }
}
