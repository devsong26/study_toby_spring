package user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleConnectionMaker {

    private final static String DB_URI = "jdbc:mysql://localhost:3306/toby_spring?serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final static String USERNAME = "user";
    private final static String PASSWORD = "123123";

    public Connection makeNewConnection() throws ClassNotFoundException, SQLException{
        Class.forName("com.mysql.jdbc.Driver");
        Connection c = DriverManager.getConnection(DB_URI, USERNAME, PASSWORD);

        return c;
    }

}
