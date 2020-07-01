package com.panprest.elastic.api.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
	private String userId;
    private String name;
    private Date creationDate = new Date();
    private Map<String, String> userSettings = new HashMap<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(Map<String, String> userSettings) {
        this.userSettings = userSettings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", creationDate=").append(creationDate);
        sb.append(", userSettings=").append(userSettings);
        sb.append('}');
        return sb.toString();
    }
}
