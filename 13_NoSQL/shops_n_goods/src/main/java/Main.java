import DataBase.DataBase;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Log4j2
public class Main {

    private static final String ADD_SHOP_REGEX = "ДОБАВИТЬ_МАГАЗИН\\s[А-я\\w]+\\s*";
    private static final String ADD_GOODS_REGEX = "ДОБАВИТЬ_ТОВАР\\s[А-я\\w]+\\s\\d{1,19}";
    private static final String SUBMIT_GOODS_REGEX = "ВЫСТАВИТЬ_ТОВАР\\s[А-я\\w]+\\s[А-я\\w]+";
    private static final String STATISTICS = "СТАТИСТИКА_ТОВАРОВ";
    private static final String EXIT = "ВЫХОД";
    private static boolean stop;

    public static void main(String[] args) {

        DataBase dataBase = new DataBase();

        while (!stop) {

            System.out.print("Введите запрос: \r\n");
            String input = readLine();

            if (input.toUpperCase().matches(EXIT)) {
                stop = true;

            } else if (input.toUpperCase().matches(ADD_SHOP_REGEX)) {
                dataBase.addShop(input);

            } else if (input.toUpperCase().matches(ADD_GOODS_REGEX)) {
                dataBase.addGoods(input);

            } else if (input.toUpperCase().matches(SUBMIT_GOODS_REGEX)) {
                dataBase.submitGoods(input);

            } else if (input.toUpperCase().matches(STATISTICS)) {
                dataBase.printStat();

            } else {
                log.info("Неверный запрос");
            }
        }
    }

    private static String readLine() {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}