package user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDaoJdbc userDao;

    @Autowired
    private DataSource dataSource;

    List<User> users;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    public void setUp(){
        users = Arrays.asList(
            new User("a", "a", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER -1, 0),
            new User("b", "b", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
            new User("c", "c", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD - 1),
            new User("d", "d", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD),
            new User("e", "e", "p5", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void upgradeLevels() throws Exception {
        userDao.deleteAll();
        for(User user: users) userDao.add(user);

        userService.upgradeLevels();

        checkLevelUpgrade(users.get(0), false);
        checkLevelUpgrade(users.get(1), true);
        checkLevelUpgrade(users.get(2), false);
        checkLevelUpgrade(users.get(3), true);
        checkLevelUpgrade(users.get(4), false);
    }

    private void checkLevelUpgrade(User user, boolean upgraded){
        User userUpdate = userDao.get(user.getId());
        if(upgraded){
            assertEquals(userUpdate.getLevel(), user.getLevel().nextLevel());
        }
        else{
            assertEquals(userUpdate.getLevel(), user.getLevel());
        }
    }

    @Test
    public void add(){
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertEquals(userWithLevelRead.getLevel(), userWithLevel.getLevel());
        assertEquals(userWithoutLevelRead.getLevel(), userWithoutLevel.getLevel());
    }

    static class TestUserService extends UserService{
        private String id;
        private TestUserService(String id){
            this.id = id;
        }
        protected void upgradeLevel(User user){
            if(user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException{}

    @Test
    public void upgradeAllOrNothing() {
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setTransactionManager(this.transactionManager);

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try{
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch(TestUserServiceException e){}

        checkLevelUpgrade(users.get(1), false);
    }

}