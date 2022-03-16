package user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    User user;

    @BeforeEach
    public void setUp(){
        user = new User();
    }

    @Test
    public void upgradeLevel(){
        Level[] levels = Level.values();
        for(Level level : levels){
            if(level.nextLevel() == null) continue;
            user.setLevel(level);
            user.upgradeLevel();
            assertEquals(user.getLevel(), level.nextLevel());
        }
    }

    @Test
    public void cannotUpgradeLevel(){
        Level[] levels = Level.values();
        assertThrows(IllegalStateException.class, () -> {
            for(Level level : levels){
                if(level.nextLevel() != null) continue;
                user.setLevel(level);
                user.upgradeLevel();
            }
        });
    }

}