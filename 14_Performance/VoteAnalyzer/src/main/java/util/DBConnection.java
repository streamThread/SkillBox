package util;

import java.sql.*;

public class DBConnection {

    private StringBuilder insertQuery = new StringBuilder();

    private static final String DB_NAME = "learn";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234567u";
    private Connection connection;

    public DBConnection() {
        setConnection();
    }

    private void setConnection() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false" +
                            "&user=" + DB_USER + "&password=" + DB_PASS + "&serverTimezone=UTC");
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS voter_count");
                statement.execute("CREATE TABLE voter_count(" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "name TINYTEXT NOT NULL, " +
                        "birthDate DATE NOT NULL, " +
                        "`count` INT NOT NULL, " +
                        "PRIMARY KEY(id), " +
                        "UNIQUE KEY name_date(name(50), birthDate))");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void executeMultiInsert() throws SQLException {
        String sql = "INSERT INTO voter_count(name, birthDate, `count`) " +
                "VALUES" + insertQuery.toString() + " " +
                "ON DUPLICATE KEY UPDATE `count` = `count` + 1";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        insertQuery = new StringBuilder();
    }

    public void countVoter(String name, String birthDay) throws SQLException {
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
