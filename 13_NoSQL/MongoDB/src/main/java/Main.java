import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.opencsv.bean.CsvToBeanBuilder;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Main {
    public static void main(String[] args) {
        List<Student> studentBeans = new CsvToBeanBuilder<Student>(
                new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(
                                        Main.class.getClassLoader().getResourceAsStream("mongo.csv")))))
                .withType(Student.class).build().parse();

        MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoDatabase database = mongoClient.getDatabase("local").withCodecRegistry(pojoCodecRegistry);

        MongoCollection<Student> collection = database.getCollection("TestSkillDemo", Student.class);

        collection.drop();

        collection.insertMany(studentBeans);

        MongoCollection<Student> collectionFromDB = database.getCollection("TestSkillDemo", Student.class);

        System.out.printf("— общее количество студентов в базе: %d\r\n",
                collectionFromDB.countDocuments());

        System.out.printf("— количество студентов старше 40 лет: %d\r\n",
                collectionFromDB.countDocuments(Filters.gt("age", 40)));

        System.out.printf("- имя самого молодого студента: %s \r\n",
                Objects.requireNonNull(collectionFromDB.find()
                        .sort(Sorts.ascending("age")).first()).getName());

        System.out.print("- список курсов самого старого студента: ");
        Objects.requireNonNull(collectionFromDB.find()
                .sort(Sorts.descending("age")).first())
                .getCourses().forEach(a -> System.out.print(a + " "));
    }
}
