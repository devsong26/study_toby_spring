package user.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import user.domain.Message;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/FactoryBeanTest-context.xml")
public class MessageFactoryBeanTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void getMessageFromFactoryBean(){
        Object message = context.getBean("message");
        assertEquals(message.getClass(), Message.class);
        assertEquals(((Message)message).getText(), "Factory Bean");
    }

    @Test
    public void getFactoryBean(){
        Object factory = context.getBean("&message"); // &가 붙고 안 붙고에 따라 getBean() 메서드가 돌려주는 오브젝트가 달라진다.
        assertEquals(factory.getClass(), MessageFactoryBean.class);
    }

}