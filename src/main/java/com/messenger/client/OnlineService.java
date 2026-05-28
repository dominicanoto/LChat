package com.messenger.client;

import java.util.HashMap;
import java.util.Map;

public class OnlineService {

    private static final Map<String, Boolean>
            onlineUsers =
            new HashMap<>();

    public static void setOnline(
            String username,
            boolean online
    ) {

        onlineUsers.put(
                username,
                online
        );
    }

    public static boolean isOnline(
            String username
    ) {

        return onlineUsers.getOrDefault(
                username,
                false
        );
    }
}