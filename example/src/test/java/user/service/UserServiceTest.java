package user.service;

import jdk.ReflectionTest.TransactionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDaoJdbc userDao;

    @Autowired
    private DataSource dataSource;

    List<User> users;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MailSender mailSender;

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
        //고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 된다.
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        // 다이내믹한 목 오브젝트 생성과 메서드의 리턴 값 설정, 그리고 DI까지 세 줄이면 층분하다.
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // 리턴값이 ㅇ벗는 메서드를 가진 목 오브젝트는 더욱 간단하게 만들 수 있다.
        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        // 테스트 대상 실행
        userServiceImpl.upgradeLevels();

        // 목 오브젝트가 제공하는 검증 기능을 통해서 어떤 메서드가 몇 번 호출됐는지, 파라미터는 무엇인지 확인할 수 있다.
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

    // id와 level을 확인하는 간단한 헬퍼 메서드
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

        userServiceImpl.add(userWithLevel);
        userServiceImpl.add(userWithoutLevel);

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
    public void upgradeAllOrNothing() {
        UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setTransactionManager(this.transactionManager);
        testUserService.setMailSender(this.mailSender);

        // 트랜잭션 핸들러가 필요한 정보와 오브젝트를 DI 해준다.
        TransactionHandler txHandler = new TransactionHandler();
        txHandler.setTarget(testUserService);
        txHandler.setTransactionManager(transactionManager);
        txHandler.setPattern("upgradeLevels");

        // UserService 인터페이스 타입의 다이내믹 프록시 생성
        UserService txUserService = (UserService) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class[]{UserService.class}, txHandler);

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try{
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch(TestUserServiceException e){}

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

}