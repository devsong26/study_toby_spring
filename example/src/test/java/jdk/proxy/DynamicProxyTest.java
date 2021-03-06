package jdk.proxy;

import jdk.ReflectionTest;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicProxyTest {

    @Test
    public void simpleProxy(){
        Hello proxyHello = (Hello) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {Hello.class},
            new ReflectionTest.UppercaseHandler(new HelloTarget()));
    }

    @Test
    public void proxyFactoryBean(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        pfBean.addAdvice(new UppercaseAdvice());

        Hello proxyHello = (Hello) pfBean.getObject();
        assertEquals(proxyHello.sayHello("Toby"), "HELLO TOBY");
        assertEquals(proxyHello.sayHi("Toby"), "HI TOBY");
        assertEquals(proxyHello.sayThankYou("Toby"), "THANK YOU TOBY");
    }

    static class UppercaseAdvice implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            String ret = (String) invocation.proceed();
            return ret.toUpperCase();
        }
    }

    interface Hello {
        String sayHello(String name);
        String sayHi(String name);
        String sayThankYou(String name);
    }

    class HelloTarget implements Hello {

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
    public void pointcutAdvisor(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut(); // ????????? ????????? ???????????? ????????? ???????????? ??????????????? ???????????? ???????????? ??????
        pointcut.setMappedName("sayH*"); //?????? ???????????? ??????. sayH??? ???????????? ?????? ???????????? ???????????? ??????.

        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice())); //??????????????? ?????????????????? Advisor??? ????????? ??? ?????? ??????

        Hello proxyHello = (Hello) pfBean.getObject();

        assertEquals(proxyHello.sayHello("Toby"), "HELLO TOBY");
        assertEquals(proxyHello.sayHi("Toby"), "HI TOBY");
        assertEquals(proxyHello.sayThankYou("Toby"), "Thank You Toby");
    }

    @Test
    void classNamePointcutAdvisor() {
        NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut(){
          public ClassFilter getClassFilter(){
              return clazz -> clazz.getSimpleName().startsWith("HelloT");
          }
        };

        classMethodPointcut.setMappedName("sayH*");

        //?????????
        checkAdviced(new HelloTarget(), classMethodPointcut, true);

        class HelloWorld extends HelloTarget {};
        checkAdviced(new HelloWorld(), classMethodPointcut, false);

        class HelloToby extends HelloTarget {};
        checkAdviced(new HelloToby(), classMethodPointcut, true);
    }

    private void checkAdviced(Object target, Pointcut pointcut, boolean isAdvice){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(target);
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
        Hello proxyHello = (Hello) pfBean.getObject();

        if(isAdvice){
            assertEquals(proxyHello.sayHello("Toby"), "HELLO TOBY");
            assertEquals(proxyHello.sayHi("Toby"), "HI TOBY");
            assertEquals(proxyHello.sayThankYou("Toby"), "Thank You Toby");
        }else{
            assertEquals(proxyHello.sayHello("Toby"), "Hello Toby");
            assertEquals(proxyHello.sayHi("Toby"), "Hi Toby");
            assertEquals(proxyHello.sayThankYou("Toby"), "Thank You Toby");
        }
    }
}
