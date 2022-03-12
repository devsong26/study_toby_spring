package user.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class DaoFactory{

    @Value("${db.uri}")
    private String uri;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Bean
    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setDataSource(dataSource());
        return userDao;
    }

//    public AccountDao accountDao(){
//        return new AccountDao(connectionMaker());
//    }
//
//    public MessageDao messageDao(){
//        return new MessageDao(connectionMaker());
//    }

    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

        dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        dataSource.setUrl(uri);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

}
