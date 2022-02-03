package user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import user.domain.User;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

//    public UserDao(ConnectionMaker connectionMaker){
//        this.connectionMaker = connectionMaker;
//    }

    public void add(User user) throws SQLException {
        Connection c = dataSource.getConnection();

        final String insertQuery = "insert into users(id, name, password) values (?,?,?)";
        PreparedStatement ps = c.prepareStatement(insertQuery);
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws SQLException{
        Connection c = dataSource.getConnection();

        final String selectQuery = "select * from users where id = ?";
        PreparedStatement ps = c.prepareStatement(selectQuery);
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();

        return user;
    }

//    private Connection getConection() throws ClassNotFoundException, SQLException {
//        Class.forName("com.mysql.jdbc.Driver");
//        Connection c = DriverManager.getConnection(DB_URI, USERNAME, PASSWORD);
//
//        return c;
//    }

}
