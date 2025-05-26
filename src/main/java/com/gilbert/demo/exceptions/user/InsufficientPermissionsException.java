package com.gilbert.demo.exceptions.user;

public class InsufficientPermissionsException extends RuntimeException {

    private final String userId;
    private final String requiredPermission;
    private final String action;

    public InsufficientPermissionsException(String userId, String action) {
        super(String.format("User %s lacks permission to perform action: %s", userId, action));
        this.userId = userId;
        this.action = action;
        this.requiredPermission = null;
    }

    public InsufficientPermissionsException(String userId, String action, String requiredPermission) {
        super(String.format("User %s lacks permission '%s' to perform action: %s",
                userId, requiredPermission, action));
        this.userId = userId;
        this.action = action;
        this.requiredPermission = requiredPermission;
    }

    public InsufficientPermissionsException(String message, String userId, String action, String requiredPermission) {
        super(message);
        this.userId = userId;
        this.action = action;
        this.requiredPermission = requiredPermission;
    }

    public String getUserId() {
        return userId;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getAction() {
        return action;
    }
}