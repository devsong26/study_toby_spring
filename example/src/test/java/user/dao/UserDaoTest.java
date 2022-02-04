package user.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import user.domain.User;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserDaoTest {

    private UserDao dao;

    @BeforeEach
    public void setUp(){
        ApplicationContext context =
                new AnnotationConfigApplicationContext(DaoFactory.class);
        dao = context.getBean("userDao", UserDao.class);
    }

    @AfterEach
    public void tearDown() throws SQLException{
        dao.deleteAll();
    }

    @Test
    public void test_addAndGet() throws SQLException {
        User user1 = new User("1", "a", "b");
        User user2 = new User("2", "c", "d");

        dao.deleteAll();
        assertEquals(dao.getCount(), 0);

        dao.add(user1);
        dao.add(user2);
        assertEquals(dao.getCount(), 2);

        User userGet1 = dao.get(user1.getId());
        assertEquals(userGet1.getName(), user1.getName());
        assertEquals(userGet1.getPassword(), user1.getPassword());

        User userGet2 = dao.get(user2.getId());
        assertEquals(userGet2.getName(), user2.getName());
        assertEquals(userGet2.getPassword(), user2.getPassword());
    }

    @Test
    public void test_getCount() throws SQLException {
        User user1 = new User("1", "a", "b");
        User user2 = new User("2", "c", "d");
        User user3 = new User("3", "e", "f");

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

}
