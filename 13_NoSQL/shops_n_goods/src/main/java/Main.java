import Model.Good;
import Model.Shop;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;

import static com.mongodb.client.model.Updates.addToSet;

@Log4j2
public class Main {

    private static final String ADD_SHOP_REGEX = "ДОБАВИТЬ_МАГАЗИН\\s[А-я\\w]+\\s*";
    private static final String ADD_GOODS_REGEX = "ДОБАВИТЬ_ТОВАР\\s[А-я\\w]+\\s\\d{1,19}";
    private static final String SUBMIT_GOODS_REGEX = "ВЫСТАВИТЬ_ТОВАР\\s[А-я\\w]+\\s[А-я\\w]+";
    private static final String STATISTICS = "СТАТИСТИКА_ТОВАРОВ";
    private static final String EXIT = "ВЫХОД";
    private static final MongoDatabase DATABASE = MongoConnect.initDB();
    private static boolean stop;

    public static void main(String[] args) {

//        DATABASE.drop();
        IndexOptions indexOptions = new IndexOptions().unique(true);
        DATABASE.getCollection("shop").createIndex(Indexes.text("name"), indexOptions);
        DATABASE.getCollection("good").createIndex(Indexes.text("name"), indexOptions);

        while (!stop) {

            System.out.print("Введите запрос: \r\n");
            String input = readLine();

            if (input.toUpperCase().matches(EXIT)) {
                stop = true;

            } else if (input.toUpperCase().matches(ADD_SHOP_REGEX)) {
                addShop(input);

            } else if (input.toUpperCase().matches(ADD_GOODS_REGEX)) {
                addGoods(input);

            } else if (input.toUpperCase().matches(SUBMIT_GOODS_REGEX)) {
                submitGoods(input);

            } else if (input.toUpperCase().matches(STATISTICS)) {

                Document document = DATABASE.getCollection("shop").aggregate(
                        Arrays.asList(Aggregates.lookup(
                                "good", "goodsSet", "name", "goodsList"),
                                Aggregates.project(Projections.excludeId())))
                        .cursor().next();

                System.out.println(document);

                Document document2 = DATABASE.getCollection("shop").find()
                        .cursor().next();

                System.out.println(document2);

                FindIterable<Shop> shopFindIterable = DATABASE.getCollection("shop", Shop.class).find();
                if (shopFindIterable.cursor().hasNext()) {
                    System.out.println(shopFindIterable.cursor().next().toString());
                }
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

    private static void addShop(String input) {
        String[] inputArray = input.strip().split("\\s");
        Shop shop = new Shop(inputArray[1], new HashSet<>());
        try {
            DATABASE.getCollection("shop", Shop.class).insertOne(shop, new InsertOneOptions());
        } catch (MongoWriteException ex) {
            log.info("Магазин с таким названием уже существует в базе. Магазин не добавлен");
            return;
        }
        log.info(String.format("Магазин %s добавлен в базу данных", inputArray[1]));
    }

    private static void addGoods(String input) {
        String[] inputArray = input.strip().split("\\s");
        Good item = new Good(inputArray[1], Integer.parseInt(inputArray[2]));
        try {
            DATABASE.getCollection("good", Good.class).insertOne(item);
        } catch (MongoWriteException ex) {
            log.info("Товар с таким наименованием уже существует в базе. Товар не добавлен");
            return;
        }
        log.info(String.format("Товар %s добавлен в базу данных", inputArray[1]));
    }

    private static void submitGoods(String input) {
        String[] inputArray = input.strip().split("\\s");
        boolean fail = false;
        Good good = DATABASE.getCollection("good", Good.class)
                .find(Filters.eq("name", inputArray[1])).first();
        if (good == null) {
            log.info(String.format("Товар %s не найден в базе данных", inputArray[1]));
            fail = true;
        }
        Shop shop = DATABASE.getCollection("shop", Shop.class)
                .find(Filters.eq("name", inputArray[2])).first();
        if (shop == null) {
            log.info(String.format("Магазин %s не найден в базе данных", inputArray[2]));
            fail = true;
        }
        if (fail) {
            return;
        }
        DATABASE.getCollection("shop").findOneAndUpdate(
                Filters.eq("name", inputArray[2]),
                addToSet("goodsSet", good.getName()));
        log.info(String.format("Товар %s выставлен на витрину в магазине %s", inputArray[1], inputArray[2]));
    }
}
