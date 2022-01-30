package user.dao;

import user.domain.User;

import java.sql.*;

public class UserDao {

    private final static String DB_URI = "jdbc:mysql://localhost:3306/toby_spring?serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final static String USERNAME = "user";
    private final static String PASSWORD = "123123";

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = getConection();

        final String insertQuery = "insert into users(id, name, password) values (?,?,?)";
        PreparedStatement ps = c.prepareStatement(insertQuery);
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException{
        Connection c = getConection();

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

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        UserDao dao = new UserDao();

        User user = new User();
        user.setId("whiteship");
        user.setName("backkiseon");
        user.setPassword("married");

        dao.add(user);

        System.out.println(user.getId() + " add success");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());

        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " select success");
    }

    private Connection getConection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection c = DriverManager.getConnection(DB_URI, USERNAME, PASSWORD);

        return c;
    }

}
