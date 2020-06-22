package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.sql.*;

public class DBConnection implements AutoCloseable {

    private StringBuilder insertQuery = new StringBuilder();
    private static final Logger logger = LogManager.getLogger();
    private static final Marker INFO = MarkerManager.getMarker("INFO");
    private static final String DB_NAME = "learn";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234567u";

    private int linesCount;
    private Connection connection;

    public DBConnection() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false" +
                            "&user=" + DB_USER + "&password=" + DB_PASS + "&serverTimezone=UTC");
            linesCount = 0;
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS voter_count");
            statement.execute("CREATE TABLE voter_count(" +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name TINYTEXT NOT NULL, " +
                    "birthDate DATE NOT NULL, " +
                    "`count` TINYINT UNSIGNED NOT NULL, " +
                    "PRIMARY KEY(id), " +
                    "UNIQUE KEY name_date(name(50), birthDate), " +
                    "KEY count_key USING BTREE(`count`))");
        } catch (SQLException exception) {
            logger.error(exception);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            logger.error(throwables);
        }
    }

    public void setLinesCount(int linesCount) {
        this.linesCount = linesCount;
    }

    public void executeMultiInsert() {
        String sql = "INSERT INTO voter_count(name, birthDate, `count`) " +
                "VALUES" + insertQuery.toString() + " " +
                "ON DUPLICATE KEY UPDATE `count` = `count` + 1";
        try (Statement statement = connection.createStatement()) {
            linesCount = linesCount + statement.executeUpdate(sql);
            if (logger.isInfoEnabled() && linesCount != 0) {
                logger.info(INFO, String.format("Обработано %d строк", linesCount));
            }
        } catch (SQLException exception) {
            logger.error(exception);
        }
        insertQuery = new StringBuilder();
    }

    public void countVoter(String name, String birthDay) {
        birthDay = birthDay.replace('.', '-');
        String newQueryPart = "('" + name + "', '" + birthDay + "', 1)";
        if (insertQuery.length() * 2 + newQueryPart.length() * 2 + 104 * 2 > 4194304) {
            executeMultiInsert();
        }
        insertQuery.append(insertQuery.length() == 0 ? "" : ", ").append(newQueryPart);
    }

    public String getStringOfVoterCounts() throws SQLException {
        String sql = "SELECT name, birthDate, `count` FROM voter_count WHERE `count` > 1";
        try (ResultSet rs = connection.createStatement().executeQuery(sql)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\r\n");
            while (rs.next()) {
                stringBuilder
                        .append('\t').append(rs.getString("name"))
                        .append(" (").append(rs.getString("birthDate"))
                        .append(") - ").append(rs.getInt("count")).append("\r\n");
            }
            return stringBuilder.toString();
        }
    }
}
