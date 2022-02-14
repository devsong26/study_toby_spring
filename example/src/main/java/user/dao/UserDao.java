package user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import user.domain.User;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void add(User user) throws SQLException {
        jdbcContextWithStatementStrategy(
            c -> {
                PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
                ps.setString(1, user.getId());
                ps.setString(2,user.getName());
                ps.setString(3, user.getPassword());

                return ps;
            }
        );
    }

    public User get(String id) throws SQLException{
        Connection c = dataSource.getConnection();

        final String selectQuery = "select * from users where id = ?";
        PreparedStatement ps = c.prepareStatement(selectQuery);
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        User user = null;
        if(rs.next()){
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        rs.close();
        ps.close();
        c.close();

        if(user == null){
            throw new EmptyResultDataAccessException(1);
        }

        return user;
    }

    public void deleteAll() throws SQLException{
        jdbcContextWithStatementStrategy(c -> c.prepareStatement("delete from users"));
    }

    public int getCount() throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            c = dataSource.getConnection();
            ps = c.prepareStatement("select count(*) from users");
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch(SQLException e) {
            throw e;
        } finally {
            if(rs != null){
                try{
                    rs.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if(ps != null){
                try{
                    ps.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if(c != null){
                try{
                    c.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = dataSource.getConnection();

            ps = stmt.makePreparedStatement(c);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            }
            if(c != null){
                try {
                    c.close();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
