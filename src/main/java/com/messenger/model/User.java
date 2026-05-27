package com.messenger.model;

public class User {

    private String name;

    private String username;

    public User(
            String name,
            String username
    ) {

        this.name = name;
        this.username = username;
    }

    public String getName() {

        return name;

    }

    public String getUsername() {

        return username;

    }

    @Override
    public String toString() {

        return name + " (@" + username + ")";

    }
}