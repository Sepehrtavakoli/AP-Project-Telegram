package org.example.database;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.example.model.User;
import org.example.projectbackend.Message;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:data/telegram.db";
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    public static void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Disconnected from database.");
            }
        } catch (SQLException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public static void initializeDatabase() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        connect();

        if (connection == null) {
            System.err.println("Database connection is null. Cannot initialize tables.");
            return;
        }

        String[] createTables = {
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id TEXT PRIMARY KEY, " +
                        "phone_number TEXT UNIQUE NOT NULL, " +
                        "first_name TEXT NOT NULL, " +
                        "last_name TEXT, " +
                        "avatar_path TEXT, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",

                "CREATE TABLE IF NOT EXISTS private_messages (" +
                        "message_id TEXT PRIMARY KEY, " +
                        "sender_id TEXT NOT NULL, " +
                        "receiver_id TEXT, " +  // ❌ قدیمی: "receiver_id TEXT NOT NULL, "
                        "content TEXT NOT NULL, " +  // ✅ جدید: "receiver_id TEXT, "
                        "message_type TEXT NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (sender_id) REFERENCES users(user_id), " +
                        "FOREIGN KEY (receiver_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS groups (" +
                        "group_id TEXT PRIMARY KEY, " +
                        "group_name TEXT NOT NULL, " +
                        "creator_id TEXT NOT NULL, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (creator_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS group_members (" +
                        "group_id TEXT NOT NULL, " +
                        "user_id TEXT NOT NULL, " +
                        "joined_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (group_id, user_id), " +
                        "FOREIGN KEY (group_id) REFERENCES groups(group_id), " +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS group_messages (" +
                        "message_id TEXT PRIMARY KEY, " +
                        "group_id TEXT NOT NULL, " +
                        "sender_id TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "message_type TEXT NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (group_id) REFERENCES groups(group_id), " +
                        "FOREIGN KEY (sender_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS channels (" +
                        "channel_id TEXT PRIMARY KEY, " +
                        "channel_name TEXT NOT NULL, " +
                        "owner_id TEXT NOT NULL, " +
                        "is_public BOOLEAN DEFAULT TRUE, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (owner_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS channel_subscribers (" +
                        "channel_id TEXT NOT NULL, " +
                        "user_id TEXT NOT NULL, " +
                        "subscribed_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (channel_id, user_id), " +
                        "FOREIGN KEY (channel_id) REFERENCES channels(channel_id), " +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS channel_posts (" +
                        "post_id TEXT PRIMARY KEY, " +
                        "channel_id TEXT NOT NULL, " +
                        "sender_id TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "post_type TEXT NOT NULL, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (channel_id) REFERENCES channels(channel_id), " +
                        "FOREIGN KEY (sender_id) REFERENCES users(user_id))"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String table : createTables) {
                stmt.execute(table);
            }
            System.out.println("All tables created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static boolean addUser(User user) {
        if (connection == null) {
            System.err.println("Database connection is null. Cannot add user.");
            return false;
        }

        String sql = "INSERT OR REPLACE INTO users (user_id, phone_number, first_name, last_name, avatar_path) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId().toString());
            pstmt.setString(2, user.getPhoneNumber());
            pstmt.setString(3, user.getFirstName());
            pstmt.setString(4, user.getLastName());
            pstmt.setString(5, user.getAvatarPath());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected); // اضافه شد
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace(); // خطای کامل رو چاپ کن
            return false;
        }
    }

    public static User getUserByPhone(String phoneNumber) {
        if (connection == null) {
            System.err.println("Database connection is null.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE phone_number = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(UUID.fromString(rs.getString("user_id")));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setAvatarPath(rs.getString("avatar_path"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    public static User getUserById(UUID userId) {
        if (connection == null) {
            System.err.println("Database connection is null.");
            return null;
        }

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(UUID.fromString(rs.getString("user_id")));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setAvatarPath(rs.getString("avatar_path"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        return null;
    }

    // در DatabaseHelper این متدها رو اضافه کن:
    public static boolean savePrivateMessage(Message message) {
        if (connection == null) return false;

        String sql = "INSERT INTO private_messages (message_id, sender_id, content, message_type) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessageId().toString());
            pstmt.setString(2, message.getSenderId().toString());
            pstmt.setString(3, message.getContent());
            pstmt.setString(4, message.getType().toString());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
            return false;
        }
    }



    public static List<Message> getPrivateMessages(UUID user1, UUID user2) {
        List<Message> messages = new ArrayList<>();
        if (connection == null) return messages;

        String sql = "SELECT * FROM private_messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user1.toString());
            pstmt.setString(2, user2.toString());
            pstmt.setString(3, user2.toString());
            pstmt.setString(4, user1.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(UUID.fromString(rs.getString("message_id")));
                message.setSenderId(UUID.fromString(rs.getString("sender_id")));
                message.setContent(rs.getString("content"));
                message.setType(Message.MessageType.valueOf(rs.getString("message_type")));
                // timestamp رو هم اگر نیاز داری set کن

                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting messages: " + e.getMessage());
        }
        return messages;
    }
}