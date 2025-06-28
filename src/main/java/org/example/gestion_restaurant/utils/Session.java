package org.example.gestion_restaurant.utils;

import org.example.gestion_restaurant.models.User;

public class Session {
    private static Session instance;
    private User currentUser;
    private String sessionId;
    private long loginTime;

    private Session() {
        this.loginTime = System.currentTimeMillis();
        this.sessionId = generateSessionId();
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(User user) {
        this.currentUser = user;
        this.loginTime = System.currentTimeMillis();
        this.sessionId = generateSessionId();
    }

    public User getUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - loginTime;
    }

    public void clear() {
        currentUser = null;
        sessionId = null;
        loginTime = 0;
    }

    private String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_" + 
               (currentUser != null ? currentUser.getId() : "ANONYMOUS");
    }

    public void refreshSession() {
        this.loginTime = System.currentTimeMillis();
        this.sessionId = generateSessionId();
    }

    @Override
    public String toString() {
        if (currentUser != null) {
            return String.format("Session{user=%s, duration=%d ms}", 
                               currentUser.getUsername(), getSessionDuration());
        }
        return "Session{no user logged in}";
    }
}