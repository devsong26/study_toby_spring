package user.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnectionMaker {

    Connection makeConnection() throws ClassNotFoundException, SQLException;

    enum DBInfo {
        DB_URI("jdbc:mysql://localhost:3306/toby_spring?serverTimezone=UTC&allowPublicKeyRetrieval=true"),
        USERNAME("user"),
        PASSWORD("123123")
        ;

        private String value;

        DBInfo(String value){
            this.value = value;
        }

        public static Connection getConnection() throws ClassNotFoundException, SQLException {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URI.value, USERNAME.value, PASSWORD.value);
        }
    }

}
