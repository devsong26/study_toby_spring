package junit;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JUnitTest {
    static Set<JUnitTest> testObjects = new HashSet<>();
//    static JUnitTest testObject;

    @Test
    public void test1(){
        assertThat(testObjects, not(hasItem(this)));
        testObjects.add(this);
    }

    @Test
    public void test2(){
        assertThat(testObjects, not(hasItem(this)));
        testObjects.add(this);
    }

    @Test
    public void test3(){
        assertThat(testObjects, not(hasItem(this)));
        testObjects.add(this);
    }

}
