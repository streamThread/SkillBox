import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    private static final String DB_NAME = "learn";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234567u";

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/" + DB_NAME +
                                "?user=" + DB_USER + "&password=" + DB_PASS);
                connection.createStatement().execute("DROP TABLE IF EXISTS voter_count");
                connection.createStatement().execute("CREATE TABLE voter_count(" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "name TINYTEXT NOT NULL, " +
                        "birthDate DATE NOT NULL, " +
                        "`count` INT NOT NULL, " +
                        "PRIMARY KEY(id))");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void countVoter(String name, String birthDay) throws SQLException
    {
        birthDay = birthDay.replace('.', '-');
        String sql = "SELECT id FROM voter_count WHERE birthDate='" + birthDay + "' AND name='" + name + "'";
        ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
        if(!rs.next())
        {
            DBConnection.getConnection().createStatement()
                    .execute("INSERT INTO voter_count(name, birthDate, `count`) VALUES('" +
                            name + "', '" + birthDay + "', 1)");
        }
        else {
            int id = rs.getInt("id");
            DBConnection.getConnection().createStatement()
                    .execute("UPDATE voter_count SET `count`=`count`+1 WHERE id=" + id);
        }
        rs.close();
    }

    public static void printVoterCounts() throws SQLException
    {
        String sql = "SELECT name, birthDate, `count` FROM voter_count WHERE `count` > 1";
        ResultSet rs = DBConnection.getConnection().createStatement().executeQuery(sql);
        while(rs.next())
        {
            System.out.println("\t" + rs.getString("name") + " (" +
                    rs.getString("birthDate") + ") - " + rs.getInt("count"));
        }
    }
}
