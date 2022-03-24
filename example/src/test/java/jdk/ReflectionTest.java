package jdk;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReflectionTest {

    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        //length()
        assertEquals(name.length(), 6);

        Method lengthMethod = String.class.getMethod("length");
        assertEquals((Integer)lengthMethod.invoke(name), 6);

        // charAt()
        assertEquals(name.charAt(0), 'S');

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertEquals((Character) charAtMethod.invoke(name, 0), 'S');
    }

    interface Hello {
        String sayHello(String name);
        String sayHi(String name);
        String sayThankYou(String name);
    }

    public class HelloTarget implements Hello {

        @Override
        public String sayHello(String name) {
            return "Hello " + name;
        }

        @Override
        public String sayHi(String name) {
            return "Hi " + name;
        }

        @Override
        public String sayThankYou(String name) {
            return "Thank You " + name;
        }
    }

    @Test
    public void simpleProxy(){
        Hello hello = new HelloTarget(); // 타깃은 인터페이스를 통해 접근하는 습관을 들이자
        assertEquals(hello.sayHello("Toby"), "Hello Toby");
        assertEquals(hello.sayHi("Toby"), "Hi Toby");
        assertEquals(hello.sayThankYou("Toby"), "Thank You Toby");
    }

    public class HelloUppercase implements Hello {
        Hello hello; // 위임할 타깃 오브젝트, 여기서는 타깃 클래스의 오브젝트인 것은 알지만 다른 프록시를 추가할 수도 있으므로
                    // 인터페이스로 접근하다.

        public HelloUppercase(Hello hello){
            this.hello = hello;
        }

        @Override
        public String sayHello(String name) {
            return hello.sayHello(name).toUpperCase(); // 위임과 부가기능 적용
        }

        @Override
        public String sayHi(String name) {
            return hello.sayHi(name).toUpperCase();
        }

        @Override
        public String sayThankYou(String name) {
            return hello.sayThankYou(name).toUpperCase();
        }
    }

    @Test
    public void test_proxyHello(){
        Hello proxyHello = new HelloUppercase(new HelloTarget()); // 프록시를 통해 타깃 오브젝트에 접근하도록 구성한다.
        assertEquals(proxyHello.sayHello("Toby"), "HELLO TOBY");
        assertEquals(proxyHello.sayHi("Toby"), "HI TOBY");
        assertEquals(proxyHello.sayThankYou("Toby"), "THANK YOU TOBY");
    }

    public class UppercaseHandler implements InvocationHandler {
        /**
         * 다이내믹 프록시로부터 전달받은 요청을 다시 타깃 오브젝트에 위임해야 하기 때문에 타깃 오브젝트를 주입받아 둔다.
         */
        Hello target;

        public UppercaseHandler(Hello target){
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String ret = (String)method.invoke(target, args); // 타깃으로 위임, 인터페이스의 메서드 호출에 모두 적용된다.
            return ret.toUpperCase();
        }
    }

    @Test
    public void createProxyHello(){
        final Hello proxyHello = (Hello) Proxy.newProxyInstance( // 생성된 다이내믹 프록시 오브젝트는 Hello 인터페이스를 구현하고 있으므로 Hello 타입으로 캐스팅해도 안전하다.
                getClass().getClassLoader(), // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
                new Class[] {Hello.class}, // 구현할 인터페이스
                new UppercaseHandler(new HelloTarget()) // 부가기능과 위임 코드를 담은 InvocationHandler
        );

        assertNotNull(proxyHello);
    }

}
