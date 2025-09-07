package org.example.approjectgui;

import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private static final Map<String, UUID> userCache = new HashMap<>();

    public static UUID getUserIdByName(String userName) {
        // باید از دیتابیس کاربر را پیدا کنید، نه از cache ثابت!
        User user = DatabaseHelper.getUserByName(userName);
        if (user != null) {
            return user.getUserId();
        }
        return null;
    }

    public static void addUserToCache(String userName, UUID userId) {
        userCache.put(userName, userId);
    }
}