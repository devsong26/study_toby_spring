package user.dao;

import user.domain.User;

import java.util.ArrayList;
import java.util.List;

public class MockUserDao implements UserDao{
    private List<User> users;
    private List<User> updated = new ArrayList<>();

    public MockUserDao(List<User> users){
        this.users = users;
    }

    public List<User> getUpdated() {
        return updated;
    }

    @Override
    public List<User> getAll() {
        return this.users;
    }

    @Override
    public void update(User user1) {
        updated.add(user1);
    }

    // 테스트에 사용되지 않는 메서드

    @Override
    public void deleteAll() {throw new UnsupportedOperationException();}

    @Override
    public int getCount() {throw new UnsupportedOperationException();}

    @Override
    public void add(User user) {throw new UnsupportedOperationException();}

    @Override
    public User get(String id) {throw new UnsupportedOperationException();}

}
