package DataBase;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Accumulators.last;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.lookup;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.addToSet;
import static java.util.Arrays.asList;

import Model.Good;
import Model.Shop;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.Sorts;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

public class DataBase {

  private static final Logger log =
      LogManager.getFormatterLogger(DataBase.class);
  private static final MongoDatabase DATABASE = MongoConnect.initDB();

  public DataBase() {
//        DATABASE.drop();
    IndexOptions indexOptions = new IndexOptions().unique(true);
    DATABASE.getCollection("shop")
        .createIndex(Indexes.text("name"), indexOptions);
    DATABASE.getCollection("good")
        .createIndex(Indexes.text("name"), indexOptions);
  }

  public void addShop(String input) {
    String[] inputArray = input.strip().split("\\s");
    Shop shop = new Shop(inputArray[1], new HashSet<>());
    try {
      DATABASE.getCollection("shop", Shop.class)
          .insertOne(shop, new InsertOneOptions());
    } catch (MongoWriteException ex) {
      log.info(
          "Магазин с таким названием уже существует в базе. Магазин не добавлен");
      return;
    }
    log.info("Магазин %s добавлен в базу данных", inputArray[1]);
  }

  public void addGoods(String input) {
    String[] inputArray = input.strip().split("\\s");
    Good item = new Good(inputArray[1], Integer.parseInt(inputArray[2]));
    try {
      DATABASE.getCollection("good", Good.class).insertOne(item);
    } catch (MongoWriteException ex) {
      log.info(
          "Товар с таким наименованием уже существует в базе. Товар не добавлен");
      return;
    }
    log.info("Товар %s добавлен в базу данных", inputArray[1]);
  }

  public void submitGoods(String input) {
    String[] inputArray = input.strip().split("\\s");
    boolean fail = false;
    Good good = DATABASE.getCollection("good", Good.class)
        .find(Filters.eq("name", inputArray[1])).first();
    if (good == null) {
      log.info(
          "Товар %s не найден в базе данных", inputArray[1]);
      fail = true;
    }
    Shop shop = DATABASE.getCollection("shop", Shop.class)
        .find(Filters.eq("name", inputArray[2])).first();
    if (shop == null) {
      log.info(
          "Магазин %s не найден в базе данных", inputArray[2]);
      fail = true;
    }
    if (fail) {
      return;
    }
    DATABASE.getCollection("shop").findOneAndUpdate(
        Filters.eq("name", inputArray[2]),
        addToSet("goodsSet", good.getName()));
    log.info("Товар %s выставлен на витрину в магазине %s", inputArray[1],
        inputArray[2]);
  }

  public void printStat() {

    getStat().forEach(response ->
        System.out.println(
            "Название магазина: " + response.get("_id") + "\r\n" +
                "Общее количество товаров в магазине: " + response
                .getLong("totalGoods") + "\r\n" +
                "Средняя стоимость товаров в магазине: " + Math
                .round(response.getDouble("avgPrice")) + "\r\n" +
                "Самый дешевый товар " + response.getString("cheap") + " стоит "
                + response.getInteger("cheapPrice") + "\r\n" +
                "Самый дорогой товар " + response.getString("expensive")
                + " стоит " + response.getInteger("expensivePrice") + "\r\n" +
                "Общее количество товара дешевле 100: " + response
                .getInteger("less100") + "\r\n"));
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
                sum("less100", Document.parse(
                    "{ \"$cond\": [ { \"$lt\": [ \"$goodsList.price\", 100 ] }, 1, 0 ] }")))));
  }
}
