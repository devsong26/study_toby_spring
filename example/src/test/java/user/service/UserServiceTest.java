package user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

//    @Autowired
//    private UserServiceImpl userServiceImpl;

    @Autowired
    private UserService userService;

    @Autowired
    private UserService testUserService;

    @Autowired
    private UserDaoJdbc userDao;

    @Autowired
    private DataSource dataSource;

    List<User> users;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    public void setUp(){
        users = Arrays.asList(
            new User("a", "a", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER -1, 0, "a@gmail.com"),
            new User("b", "b", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "b@gmail.com"),
            new User("c", "c", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD - 1, "c@gmail.com"),
            new User("d", "d", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD, "d@gmail.com"),
            new User("e", "e", "p5", Level.GOLD, 100, Integer.MAX_VALUE, "e@gmail.com")
        );
    }

    @Test
    public void upgradeLevels() {
        //????????? ?????????????????? ????????? ?????? ??????????????? ?????? ???????????? ??????.
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        // ??????????????? ??? ???????????? ????????? ???????????? ?????? ??? ??????, ????????? DI?????? ??? ????????? ????????????.
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // ???????????? ????????? ???????????? ?????? ??? ??????????????? ?????? ???????????? ?????? ??? ??????.
        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        // ????????? ?????? ??????
        userServiceImpl.upgradeLevels();

        // ??? ??????????????? ???????????? ?????? ????????? ????????? ?????? ???????????? ??? ??? ???????????????, ??????????????? ???????????? ????????? ??? ??????.
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertEquals(users.get(1).getLevel(), Level.SILVER);
        verify(mockUserDao).update(users.get(3));
        assertEquals(users.get(3).getLevel(), Level.GOLD);

        ArgumentCaptor<SimpleMailMessage> mailMessageArg =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertEquals(mailMessages.get(0).getTo()[0], users.get(1).getEmail());
        assertEquals(mailMessages.get(1).getTo()[0], users.get(3).getEmail());
    }

    // id??? level??? ???????????? ????????? ?????? ?????????
    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
        assertEquals(updated.getId(), expectedId);
        assertEquals(updated.getLevel(), expectedLevel);
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

    static class TestUserService extends UserServiceImpl {
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
    @DirtiesContext
    public void upgradeAllOrNothing() throws Exception {
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try{
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (TestUserServiceException e){}

        checkLevelUpgrade(users.get(1), false);
    }

    static class MockMailSender implements MailSender {
        private List<String> requests = new ArrayList<>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            requests.add(simpleMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {

        }
    }

    static class TestUserServiceImpl extends UserServiceImpl{
        private String id = "madnite1";

        protected void upgradeLevel(User user){
            if(user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    @Test
    void advisorAutoProxyCreator() {
        assertThat(testUserService, is(java.lang.reflect.Proxy.class));
    }

}