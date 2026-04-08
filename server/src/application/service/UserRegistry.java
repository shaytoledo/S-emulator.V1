package application.service;

import application.model.UserInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {

    private final ConcurrentHashMap<String, UserInfo> users = new ConcurrentHashMap<>();

    /**
     * @return the new UserInfo, or null if the username is already taken.
     */
    public UserInfo login(String username) {
        UserInfo fresh = new UserInfo(username);
        UserInfo existing = users.putIfAbsent(username, fresh);
        return existing == null ? fresh : null;
    }

    public boolean logout(String username) {
        return users.remove(username) != null;
    }

    public UserInfo getUser(String username) {
        return users.get(username);
    }

    public List<UserInfo> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }
}
