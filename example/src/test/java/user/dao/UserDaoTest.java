package user.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import user.domain.Level;
import user.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DaoFactory.class})
public class UserDaoTest {

    @Autowired
    private UserDaoJdbc dao;

    @Autowired
    private DataSource dataSource;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp(){
        user1 = new User("1", "a", "b", Level.BASIC, 1, 0);
        user2 = new User("2", "c", "d", Level.SILVER, 55, 10);
        user3 = new User("3", "e", "f", Level.GOLD, 100, 40);
    }

    @AfterEach
    public void tearDown() throws SQLException{
        dao.deleteAll();
    }

    @Test
    public void test_addAndGet() throws SQLException {
        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        dao.add(user1);
        dao.add(user2);
        assertEquals(dao.getCount(), 2);

        User userGet1 = dao.get(user1.getId());
        checkSameUser(userGet1, user1);
        assertEquals(userGet1.getName(), user1.getName());
        assertEquals(userGet1.getPassword(), user1.getPassword());

        User userGet2 = dao.get(user2.getId());
        checkSameUser(userGet2, user2);
        assertEquals(userGet2.getName(), user2.getName());
        assertEquals(userGet2.getPassword(), user2.getPassword());
    }

    @Test
    public void test_getCount() throws SQLException {
        assertEquals(dao.getCount(), 0);

        dao.add(user1);
        assertEquals(dao.getCount(), 1);

        dao.add(user2);
        assertEquals(dao.getCount(), 2);

        dao.add(user3);
        assertEquals(dao.getCount(), 3);
    }

    @Test
    public void test_getUserFailure() throws SQLException{
        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        assertThrows(EmptyResultDataAccessException.class, () -> {
            dao.get("unknown_id");
        });
    }

    @Test
    public void getAll() throws SQLException {
        dao.deleteAll();

        List<User> users0 = dao.getAll();
        assertEquals(users0.size(), 0);

        dao.add(user1);
        List<User> users1 = dao.getAll();
        assertEquals(users1.size(), 1);
        checkSameUser(user1, users1.get(0));

        dao.add(user2);
        List<User> users2 = dao.getAll();
        assertEquals(users2.size(), 2);
        checkSameUser(user1, users2.get(0));
        checkSameUser(user2, users2.get(1));

        dao.add(user3);
        List<User> users3 = dao.getAll();
        assertEquals(users3.size(), 3);
        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));
    }

    private void checkSameUser(User user1, User user2){
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getName(), user2.getName());
        assertEquals(user1.getPassword(), user2.getPassword());
        assertEquals(user1.getLevel(), user2.getLevel());
        assertEquals(user1.getLogin(), user2.getLogin());
        assertEquals(user1.getRecommend(), user2.getRecommend());
    }

    @Test
    public void duplicateKey(){
        dao.deleteAll();

        dao.add(user1);

        assertThrows(DataAccessException.class, () -> {
            dao.add(user1);
        });
    }

    @Test
    public void sqlExceptionTranslator(){
        dao.deleteAll();

        try{
            dao.add(user1);
            dao.add(user1);
        }catch(DuplicateKeyException ex){
            SQLException sqlEx = (SQLException) ex.getRootCause();
            SQLExceptionTranslator set =
                    new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertEquals(set.translate(null, null, sqlEx), DuplicateKeyException.class);
        }
    }

    @Test
    public void update(){
        dao.deleteAll();

        dao.add(user1);
        dao.add(user2);


        user1.setName("오민규");
        user1.setPassword("springbook");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);

        dao.update(user1);

        User user1update = dao.get(user1.getId());
        checkSameUser(user1, user1update);
        User user2same = dao.get(user2.getId());
        checkSameUser(user2, user2same);
    }

}
