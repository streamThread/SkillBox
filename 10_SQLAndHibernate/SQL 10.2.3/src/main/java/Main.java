import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static java.sql.DriverManager.getConnection;

public class Main {

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    private static Properties properties = new Properties();

    public static void main(String[] args) {

        try {
            properties.load(new FileInputStream("src/main/resources/sql.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            con = getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("user"),
                    properties.getProperty("password"));

            stmt = con.createStatement();

            rs = stmt.executeQuery("select course_name, " +
                    "(Count(subscription_date) / period_diff(max(extract(YEAR_MONTH from subscription_date)), min(extract(YEAR_MONTH from subscription_date)))) as AVG_registrations_count_per_month \n" +
                    "from PurchaseList\n" +
                    "group by course_name ");

            while (rs.next()) {
                System.out.printf("%.2f - Среднее количество подписок в месяц по курсу %s\n",
                        rs.getDouble(2),
                        rs.getString(1));
            }

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
            try {
                stmt.close();
            } catch (SQLException ignored) {
            }
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
