package org.example.database;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.example.model.User;
import org.example.projectbackend.Message;
import org.example.projectbackend.Channel;
import org.example.projectbackend.Group;
import java.time.LocalDateTime;
import java.util.*;

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

    public static Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connected to SQLite database.");
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return connection;  // ✅ همیشه connection معتبر یا حداقل null-safe
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
                        "FOREIGN KEY (sender_id) REFERENCES users(user_id))",

                "CREATE TABLE IF NOT EXISTS contacts (" +
                        "contact_id TEXT PRIMARY KEY, " +
                        "user_id TEXT NOT NULL, " +
                        "first_name TEXT NOT NULL, " +
                        "last_name TEXT, " +
                        "phone TEXT NOT NULL)"
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
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by phone: " + e.getMessage());
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
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by id: " + e.getMessage());
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null.");
            return users;
        }

        String sql = "SELECT * FROM users ORDER BY first_name ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }

        return users;
    }


    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(UUID.fromString(rs.getString("user_id")));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setAvatarPath(rs.getString("avatar_path"));
        return user;
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

    // 2-1) گروه‌ها
    public static UUID createGroup(String name, UUID creatorId) {
        String sql = "INSERT INTO groups (group_id, group_name, creator_id) VALUES (?, ?, ?)";
        UUID gid = UUID.randomUUID();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gid.toString());
            ps.setString(2, name);
            ps.setString(3, creatorId.toString());
            ps.executeUpdate();

            addGroupMembers(gid, java.util.List.of(creatorId));

            return gid;
        } catch (SQLException e) {
            System.err.println("createGroup error: " + e.getMessage());
            return null;
        }
    }

    public static void addGroupMembers(UUID groupId, java.util.List<UUID> members) {
        String sql = "INSERT OR IGNORE INTO group_members (group_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (UUID uid : members) {
                ps.setString(1, groupId.toString());
                ps.setString(2, uid.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("addGroupMembers error: " + e.getMessage());
        }
    }

    public static List<Group> getGroupsForUser(UUID userId) {
        String sql = """
        SELECT g.group_id, g.group_name, g.creator_id, g.created_at
        FROM groups g
        JOIN group_members gm ON gm.group_id = g.group_id
        WHERE gm.user_id = ?
        ORDER BY g.created_at DESC
    """;

        List<Group> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID gid = UUID.fromString(rs.getString("group_id"));
                    Group group = new Group(
                            gid,
                            rs.getString("group_name"),
                            UUID.fromString(rs.getString("creator_id")),
                            getGroupMembers(gid),   // 👈 اعضا رو همزمان لود کن
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    list.add(group);
                }
            }
        } catch (SQLException e) {
            System.err.println("getGroupsForUser error: " + e.getMessage());
        }
        return list;
    }



    public static boolean saveGroupMessage(UUID groupId, UUID senderId, String content, Message.MessageType type) {
        String sql = "INSERT INTO group_messages (message_id, group_id, sender_id, content, message_type) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, groupId.toString());
            ps.setString(3, senderId.toString());
            ps.setString(4, content);
            ps.setString(5, type.name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("saveGroupMessage error: " + e.getMessage());
            return false;
        }
    }

    public static java.util.List<Message> getGroupMessages(UUID groupId) {
        String sql = """
        SELECT message_id, sender_id, content, message_type, timestamp
        FROM group_messages WHERE group_id = ?
        ORDER BY datetime(timestamp)
    """;
        java.util.List<Message> messages = new java.util.ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, groupId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessageId(UUID.fromString(rs.getString("message_id")));
                    m.setSenderId(UUID.fromString(rs.getString("sender_id")));
                    m.setContent(rs.getString("content"));
                    m.setType(Message.MessageType.valueOf(rs.getString("message_type")));
                    m.setTimestamp(rs.getString("timestamp"));
                    messages.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("getGroupMessages error: " + e.getMessage());
        }
        return messages;
    }


    // 2-2) کانال‌ها
    public static UUID createChannel(String name, UUID ownerId) {
        String sql = "INSERT INTO channels (channel_id, channel_name, owner_id) VALUES (?,?,?)";
        UUID chid = UUID.randomUUID();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chid.toString());
            ps.setString(2, name);
            ps.setString(3, ownerId.toString());
            ps.executeUpdate();
            // مالک را به‌صورت پیش‌فرض subscriber کن
            subscribeToChannel(chid, ownerId);
            return chid;
        } catch (SQLException e) {
            System.err.println("createChannel error: " + e.getMessage());
            return null;
        }
    }

    public static void subscribeToChannel(UUID channelId, UUID userId) {
        String sql = "INSERT OR IGNORE INTO channel_subscribers (channel_id, user_id) VALUES (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, channelId.toString());
            ps.setString(2, userId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("subscribeToChannel error: " + e.getMessage());
        }
    }

    public static java.util.List<Channel> getOwnedChannels(UUID ownerId) {
        String sql = "SELECT channel_id, channel_name, owner_id FROM channels WHERE owner_id = ? ORDER BY created_at DESC";
        java.util.List<Channel> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ownerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Channel c = new Channel();
                    c.setChannelId(UUID.fromString(rs.getString("channel_id")));
                    c.setChannelName(rs.getString("channel_name"));
                    c.setOwnerId(UUID.fromString(rs.getString("owner_id")));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("getOwnedChannels error: " + e.getMessage());
        }
        return list;
    }

    public static java.util.List<Channel> getSubscribedChannels(UUID userId) {
        String sql = """
        SELECT c.channel_id, c.channel_name, c.owner_id
        FROM channels c
        JOIN channel_subscribers s ON s.channel_id = c.channel_id
        WHERE s.user_id = ?
        ORDER BY c.created_at DESC
    """;
        java.util.List<Channel> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Channel c = new Channel();
                    c.setChannelId(UUID.fromString(rs.getString("channel_id")));
                    c.setChannelName(rs.getString("channel_name"));
                    c.setOwnerId(UUID.fromString(rs.getString("owner_id")));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("getSubscribedChannels error: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveChannelPost(UUID channelId, UUID senderId, String content, Message.MessageType type) {
        String sql = "INSERT INTO channel_posts (post_id, channel_id, sender_id, content, message_type) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, channelId.toString());
            ps.setString(3, senderId.toString());
            ps.setString(4, content);
            ps.setString(5, type.name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("saveChannelPost error: " + e.getMessage());
            return false;
        }
    }

    public static java.util.List<Message> getChannelPosts(UUID channelId) {
        String sql = """
        SELECT post_id as message_id, sender_id, content, message_type, timestamp
        FROM channel_posts WHERE channel_id = ?
        ORDER BY datetime(timestamp)
    """;
        java.util.List<Message> messages = new java.util.ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, channelId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setMessageId(UUID.fromString(rs.getString("message_id")));
                    m.setSenderId(UUID.fromString(rs.getString("sender_id")));
                    m.setContent(rs.getString("content"));
                    m.setType(Message.MessageType.valueOf(rs.getString("message_type")));
                    m.setTimestamp(rs.getString("timestamp"));
                    messages.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("getChannelPosts error: " + e.getMessage());
        }
        return messages;
    }

    // 📌 گرفتن اعضای یک گروه به صورت لیست User
// 📌 گرفتن اعضای یک گروه به صورت لیست User
    public static List<User> getGroupMembers(UUID groupId) {
        List<User> members = new ArrayList<>();
        String sql = """
        SELECT u.user_id, u.username, u.phone_number
        FROM group_members gm
        JOIN users u ON gm.user_id = u.user_id
        WHERE gm.group_id = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, groupId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                            UUID.fromString(rs.getString("user_id")),
                            rs.getString("username"),
                            rs.getString("phone_number")
                    );
                    members.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("getGroupMembers error: " + e.getMessage());
        }
        return members;
    }




    // 📌 گرفتن لیست کانال‌های یک کاربر
    public static List<Channel> getChannelsForUser(UUID userId) {
        List<Channel> channels = new ArrayList<>();
        Connection conn = connect();
        if (conn == null) return channels;

        String sql = """
        SELECT c.channel_id, c.channel_name, c.owner_id, c.created_at
        FROM channels c
        JOIN channel_subscribers cs ON c.channel_id = cs.channel_id
        WHERE cs.user_id = ?
    """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UUID channelId = UUID.fromString(rs.getString("channel_id"));
                String channelName = rs.getString("channel_name");
                UUID ownerId = UUID.fromString(rs.getString("owner_id"));
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                List<UUID> subscribers = getChannelSubscribers(channelId);

                Channel channel = new Channel(channelId, channelName, ownerId, subscribers, createdAt);
                channels.add(channel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channels;
    }


    // 📌 گرفتن مشترکین یک کانال
    private static List<UUID> getChannelSubscribers(UUID channelId) {
        List<UUID> subs = new ArrayList<>();
        Connection conn = connect();
        if (conn == null) return subs;

        String sql = "SELECT user_id FROM channel_subscribers WHERE channel_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, channelId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                subs.add(UUID.fromString(rs.getString("user_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subs;
    }

    public static List<User> getContacts(UUID userId) {
        List<User> contacts = new ArrayList<>();
        Connection conn = connect();
        if (conn == null) return contacts;

        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.phone_number " +
                "FROM contacts c " +
                "JOIN users u ON c.contact_id = u.user_id " +
                "WHERE c.user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User u = new User(
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone_number")
                );
                contacts.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contacts;
    }


}