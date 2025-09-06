package org.example.approjectgui;

import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private static final Map<String, UUID> userCache = new HashMap<>();

    public static UUID getUserIdByName(String userName) {
        // اول از کش بررسی کنید
        if (userCache.containsKey(userName)) {
            return userCache.get(userName);
        }

        // اگر در کش نیست، از دیتابیس بگیرید
        // این قسمت نیاز به پیاده‌سازی دارد - باید از دیتابیس کاربران را بخوانید
        // به عنوان مثال موقت:
        if ("Ali Mohammadi".equals(userName)) {
            UUID id = UUID.fromString("12345678-1234-1234-1234-123456789012");
            userCache.put(userName, id);
            return id;
        }
        if ("Sara Johnson".equals(userName)) {
            UUID id = UUID.fromString("22345678-1234-1234-1234-123456789012");
            userCache.put(userName, id);
            return id;
        }

        return null;
    }

    public static void addUserToCache(String userName, UUID userId) {
        userCache.put(userName, userId);
    }
}