package com.gilbert.demo.exceptions.user;

public class UserNotActiveException extends RuntimeException {

    private final String userId;
    private final String userStatus;

    public UserNotActiveException(String userId) {
        super(String.format("User account is not active: %s", userId));
        this.userId = userId;
        this.userStatus = null;
    }

    public UserNotActiveException(String userId, String userStatus) {
        super(String.format("User account is %s: %s", userStatus, userId));
        this.userId = userId;
        this.userStatus = userStatus;
    }

    public UserNotActiveException(String message, String userId, String userStatus) {
        super(message);
        this.userId = userId;
        this.userStatus = userStatus;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserStatus() {
        return userStatus;
    }
}