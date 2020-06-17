package DataBase;

import Model.Good;
import Model.Shop;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;

import java.util.HashSet;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.addToSet;
import static java.util.Arrays.asList;

@Log4j2
public class DataBase {

    private static final MongoDatabase DATABASE = MongoConnect.initDB();

    public DataBase() {
//        DATABASE.drop();
        IndexOptions indexOptions = new IndexOptions().unique(true);
        DATABASE.getCollection("shop").createIndex(Indexes.text("name"), indexOptions);
        DATABASE.getCollection("good").createIndex(Indexes.text("name"), indexOptions);
    }

    public void addShop(String input) {
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

    public void addGoods(String input) {
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

    public void submitGoods(String input) {
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

    public void printStat() {

        getStat().forEach(response ->
                System.out.println(
                        "Название магазина: " + response.get("_id") + "\r\n" +
                                "Общее количество товаров в магазине: " + response.getLong("totalGoods") + "\r\n" +
                                "Средняя стоимость товаров в магазине: " + Math.round(response.getDouble("avgPrice")) + "\r\n" +
                                "Самый дешевый товар " + response.getString("cheap") + " стоит " + response.getInteger("cheapPrice") + "\r\n" +
                                "Самый дорогой товар " + response.getString("expensive") + " стоит " + response.getInteger("expensivePrice") + "\r\n" +
                                "Общее количество товара дешевле 100: " + response.getInteger("less100") + "\r\n"));
    }

    private Iterable<Document> getStat() {

        return DATABASE.getCollection("shop").aggregate(
                asList(
                        lookup("good", "goodsSet", "name", "goodsList"),
                        project(include("name", "goodsList")),
                        unwind("$goodsList"),
                        sort(Sorts.ascending("goodsList.price")),
                        group("$name",
                                sum("totalGoods", 1L),
                                avg("avgPrice", "$goodsList.price"),
                                first("cheap", "$goodsList.name"),
                                first("cheapPrice", "$goodsList.price"),
                                last("expensive", "$goodsList.name"),
                                last("expensivePrice", "$goodsList.price"),
                                sum("less100", Document.parse("{ \"$cond\": [ { \"$lt\": [ \"$goodsList.price\", 100 ] }, 1, 0 ] }")))));
    }
}
