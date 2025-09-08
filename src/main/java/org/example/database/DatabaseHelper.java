package org.example.database;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.example.model.Group;
import org.example.model.User;
import org.example.projectbackend.Contact;
import org.example.projectbackend.GroupMessage;
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
                        "receiver_id TEXT, " +
                        "content TEXT NOT NULL, " +
                        "message_type TEXT NOT NULL, " +
                        "delivered BOOLEAN DEFAULT FALSE, " +
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
            createContactsTable();
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
            System.out.println("Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // در کلاس DatabaseHelper.java
    public static User getUserByPhone(String phoneNumber) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        String sql = "SELECT * FROM users WHERE phone_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, phoneNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(UUID.fromString(rs.getString("user_id")));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by phone: " + e.getMessage());
        }
        return null;
    }

    // این متد جدید را به کلاس اضافه کنید

    public static List<User> getContactsAsUsers(UUID userId) {
        List<User> userContacts = new ArrayList<>();
        if (connection == null) return userContacts;

        // ابتدا ID کاربران مخاطب را پیدا می‌کنیم
        String sql = "SELECT contact_user_id FROM contacts WHERE user_id = ? AND contact_user_id IS NOT NULL";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String contactUserIdStr = rs.getString("contact_user_id");
                if (contactUserIdStr != null) {
                    User contactUser = getUserById(UUID.fromString(contactUserIdStr));
                    if (contactUser != null) {
                        userContacts.add(contactUser);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting contacts as users: " + e.getMessage());
        }
        return userContacts;
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

// این متد را به طور کامل جایگزین کنید

    public static boolean savePrivateMessage(Message message) {
        if (connection == null) {
            System.err.println("[DB_ERROR] Cannot save message, connection is null.");
            return false;
        }

        String sql = "INSERT INTO private_messages (message_id, sender_id, receiver_id, content, message_type, delivered) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessageId().toString());
            pstmt.setString(2, message.getSenderId().toString());
            pstmt.setString(3, message.getReceiverId() != null ? message.getReceiverId().toString() : null);
            pstmt.setString(4, message.getContent());
            pstmt.setString(5, message.getType().toString());
            pstmt.setBoolean(6, false);

            int rowsAffected = pstmt.executeUpdate();

            // <<-- اضافه کردن لاگ‌های دقیق برای موفقیت یا شکست
            if (rowsAffected > 0) {
                System.out.println("[DB_SUCCESS] Message from " + message.getSenderId() + " to " + message.getReceiverId() + " was saved.");
                return true;
            } else {
                System.err.println("[DB_FAILURE] Message from " + message.getSenderId() + " FAILED to save (0 rows affected).");
                return false;
            }
        } catch (SQLException e) {
            // <<-- نمایش خطای SQL به صورت بسیار واضح
            System.err.println("[DB_ERROR] SQLException while saving message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

// این متد را به طور کامل جایگزین کنید

    public static Message getLastMessage(UUID user1, UUID user2) {
        Message lastMessage = null;
        if (connection == null) {
            return null;
        }

        // کوئری تغییری نمی‌کند چون SELECT * تمام ستون‌ها را برمی‌گرداند
        String sql = "SELECT * FROM private_messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY timestamp DESC LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user1.toString());
            pstmt.setString(2, user2.toString());
            pstmt.setString(3, user2.toString());
            pstmt.setString(4, user1.toString());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                lastMessage = new Message();
                lastMessage.setSenderId(UUID.fromString(rs.getString("sender_id")));
                lastMessage.setContent(rs.getString("content"));
                // <<-- این خط مهم اضافه شده است تا نوع پیام هم خوانده شود
                lastMessage.setType(Message.MessageType.valueOf(rs.getString("message_type")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting last message: " + e.getMessage());
        }
        return lastMessage;
    }

    public static boolean updateUser(User user) {
        if (connection == null) {
            System.err.println("Database connection is null. Cannot update user.");
            return false;
        }
        // آپدیت کردن کاربر بر اساس user_id او
        String sql = "UPDATE users SET first_name = ?, last_name = ?, avatar_path = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getFirstName());
            pstmt.setString(2, user.getLastName());
            pstmt.setString(3, user.getAvatarPath());
            pstmt.setString(4, user.getUserId().toString());

            int rowsAffected = pstmt.executeUpdate();
            // اگر یک ردیف تحت تاثیر قرار گرفته باشد، یعنی آپدیت موفق بوده است
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public static List<User> getChatListUsers(UUID currentUserId) {
        if (connection == null) {
            return new ArrayList<>();
        }

        // از Set استفاده می‌کنیم تا به طور خودکار از UUID های تکراری جلوگیری شود
        java.util.Set<UUID> chatPartnerIds = new java.util.HashSet<>();

        // ۱. تمام مخاطبین کاربر را اضافه کن
        String contactsSql = "SELECT contact_user_id FROM contacts WHERE user_id = ? AND contact_user_id IS NOT NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(contactsSql)) {
            pstmt.setString(1, currentUserId.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                chatPartnerIds.add(UUID.fromString(rs.getString("contact_user_id")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contacts for chat list: " + e.getMessage());
        }

        // ۲. تمام کسانی که به کاربر پیام فرستاده‌اند را اضافه کن
        String messagesSql = "SELECT sender_id FROM private_messages WHERE receiver_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(messagesSql)) {
            pstmt.setString(1, currentUserId.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                chatPartnerIds.add(UUID.fromString(rs.getString("sender_id")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching message senders for chat list: " + e.getMessage());
        }

        // ۳. حالا برای هر UUID یکتا، آبجکت User کامل را برگردان
        List<User> chatListUsers = new ArrayList<>();
        for (UUID userId : chatPartnerIds) {
            User user = getUserById(userId);
            if (user != null) {
                chatListUsers.add(user);
            }
        }

        return chatListUsers;
    }

    public static Group getGroupById(UUID groupId) {
        String sql = "SELECT * FROM groups WHERE group_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, groupId.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Group group = new Group();
                group.setGroupId(UUID.fromString(rs.getString("group_id")));
                group.setGroupName(rs.getString("group_name"));
                group.setCreatorId(UUID.fromString(rs.getString("creator_id")));
                return group;
            }
        } catch (SQLException e) {
            System.err.println("Error getting group by ID: " + e.getMessage());
        }
        return null;
    }

// متد getPrivateMessages را به طور کامل جایگزین کنید

    public static List<Message> getPrivateMessages(UUID user1, UUID user2) {
        // --- کدهای عیب‌یابی شروع می‌شوند ---
        System.out.println("\n--- [DEBUG] Fetching messages between:");
        System.out.println("--- [DEBUG] User 1: " + user1);
        System.out.println("--- [DEBUG] User 2: " + user2);
        // ------------------------------------

        List<Message> messages = new ArrayList<>();
        if (connection == null) {
            System.out.println("[DEBUG] DB connection is null. Returning empty list.");
            return messages;
        }

        String sql = "SELECT message_id, sender_id, content, message_type, strftime('%H:%M', timestamp) as formatted_time " +
                "FROM private_messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY timestamp";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user1.toString());
            pstmt.setString(2, user2.toString());
            pstmt.setString(3, user2.toString());
            pstmt.setString(4, user1.toString());

            ResultSet rs = pstmt.executeQuery();

            int rowCount = 0; // برای شمارش ردیف‌های پیدا شده
            while (rs.next()) {
                rowCount++;
                Message message = new Message();
                message.setMessageId(UUID.fromString(rs.getString("message_id")));
                message.setSenderId(UUID.fromString(rs.getString("sender_id")));
                message.setContent(rs.getString("content"));
                message.setType(Message.MessageType.valueOf(rs.getString("message_type")));
                message.setTimestamp(rs.getString("formatted_time"));
                messages.add(message);
            }
            // --- کد عیب‌یابی ---
            System.out.println("[DEBUG] SQL query found " + rowCount + " rows.");
            // ------------------

        } catch (SQLException e) {
            System.err.println("Error getting messages: " + e.getMessage());
            e.printStackTrace(); // نمایش خطای کامل SQL
        }

        // --- کد عیب‌یابی ---
        System.out.println("[DEBUG] Returning " + messages.size() + " messages.");
        System.out.println("--- [DEBUG] Fetch complete ---\n");
        // ------------------
        return messages;
    }

    // در کلاس DatabaseHelper.java
    public static void createContactsTable() {
        if (connection == null) return;

        String sql = "CREATE TABLE IF NOT EXISTS contacts (" +
                "contact_id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "contact_user_id TEXT, " + // اینجا NOT NULL را حذف کنید
                "first_name TEXT NOT NULL, " +
                "last_name TEXT, " +
                "phone_number TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                "FOREIGN KEY (contact_user_id) REFERENCES users(user_id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Contacts table created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating contacts table: " + e.getMessage());
        }
    }

    // در کلاس DatabaseHelper.java
    public static boolean addContact(Contact contact, UUID userId) {
        if (connection == null) return false;

        String sql = "INSERT INTO contacts (contact_id, user_id, contact_user_id, first_name, last_name, phone_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, userId.toString());

            // اصلاح این خط: اگر contactUser وجود دارد، UUID آن را ذخیره کن
            pstmt.setString(3, contact.getContactUser() != null ? contact.getContactUser().getUserId().toString() : null);

            pstmt.setString(4, contact.getFirstName());
            pstmt.setString(5, contact.getLastName());
            pstmt.setString(6, contact.getPhoneNumber());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding contact: " + e.getMessage());
            return false;
        }
    }

    public static List<Contact> getContactsByUserId(UUID userId) {
        List<Contact> contacts = new ArrayList<>();
        if (connection == null) return contacts;

        String sql = "SELECT c.first_name, c.last_name, c.phone_number, c.contact_user_id FROM contacts c WHERE c.user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Contact contact = new Contact();
                contact.setFirstName(rs.getString("first_name"));
                contact.setLastName(rs.getString("last_name"));
                contact.setPhoneNumber(rs.getString("phone_number"));

                String contactUserIdStr = rs.getString("contact_user_id");
                if (contactUserIdStr != null) {
                    User contactUser = getUserById(UUID.fromString(contactUserIdStr));
                    contact.setContactUser(contactUser);
                }
                contacts.add(contact);
            }
        } catch (SQLException e) {
            System.err.println("Error getting contacts: " + e.getMessage());
        }
        return contacts;
    }

    public static boolean contactExists(UUID userId, String phoneNumber) {
        if (connection == null) return false;

        String sql = "SELECT COUNT(*) FROM contacts WHERE user_id = ? AND phone_number = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            pstmt.setString(2, phoneNumber);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking contact existence: " + e.getMessage());
            return false;
        }
    }

    public static List<Message> getPendingMessages(UUID userId) {
        List<Message> messages = new ArrayList<>();
        if (connection == null) return messages;

        String sql = "SELECT * FROM private_messages WHERE receiver_id = ? AND delivered = false ORDER BY timestamp";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(UUID.fromString(rs.getString("message_id")));
                message.setSenderId(UUID.fromString(rs.getString("sender_id")));
                message.setReceiverId(UUID.fromString(rs.getString("receiver_id")));
                message.setContent(rs.getString("content"));
                message.setType(Message.MessageType.valueOf(rs.getString("message_type")));
                message.setTimestamp(rs.getString("timestamp"));
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending messages: " + e.getMessage());
        }
        return messages;
    }

    public static User getUserByName(String userName) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }

        String sql = "SELECT * FROM users WHERE first_name = ? OR (first_name || ' ' || last_name) = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, userName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(UUID.fromString(rs.getString("user_id")));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by name: " + e.getMessage());
        }
        return null;
    }

    public static void markAsDelivered(UUID messageId) {
        if (connection == null) return;

        String sql = "UPDATE private_messages SET delivered = TRUE WHERE message_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, messageId.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking message as delivered: " + e.getMessage());
        }
    }

    // این متد را به کلاس DatabaseHelper اضافه کن
    public static boolean createGroup(String groupName, UUID creatorId, List<UUID> memberIds) {
        if (connection == null) {
            System.err.println("Database connection is null. Cannot create group.");
            return false;
        }

        // استفاده از تراکنش برای اطمینان از انجام همه INSERTها یا هیچکدام
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // شروع تراکنش

            // 1. ایجاد یک UUID جدید برای گروه
            UUID groupId = UUID.randomUUID();

            // 2. درج گروه جدید در جدول 'groups'
            String insertGroupSQL = "INSERT INTO groups (group_id, group_name, creator_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtGroup = connection.prepareStatement(insertGroupSQL)) {
                pstmtGroup.setString(1, groupId.toString());
                pstmtGroup.setString(2, groupName);
                pstmtGroup.setString(3, creatorId.toString());
                pstmtGroup.executeUpdate();
            }

            // 3. درج تمام اعضا (شامل سازنده) در جدول 'group_members'
            String insertMemberSQL = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
            try (PreparedStatement pstmtMember = connection.prepareStatement(insertMemberSQL)) {
                for (UUID memberId : memberIds) {
                    pstmtMember.setString(1, groupId.toString());
                    pstmtMember.setString(2, memberId.toString());
                    pstmtMember.addBatch(); // اضافه کردن به بچ برای اجرای جمعی
                }
                pstmtMember.executeBatch(); // اجرای تمام INSERTها با یک دستور
            }

            connection.commit(); // تأیید تراکنش
            System.out.println("Group created successfully with ID: " + groupId);
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback(); // در صورت خطا، برگرداندن تغییرات
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
            }
            System.err.println("Error creating group: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(autoCommit); // بازگرداندن حالت autoCommit به وضعیت قبلی
            } catch (SQLException e) {
                System.err.println("Error resetting autoCommit: " + e.getMessage());
            }
        }
    }

    // در کلاس DatabaseHelper.java متد زیر را اضافه کنید:
    public static List<Group> getGroupsForUser(UUID userId) {
        List<Group> groups = new ArrayList<>();
        if (connection == null) return groups;

        String sql = "SELECT g.group_id, g.group_name, g.creator_id " +
                "FROM groups g " +
                "JOIN group_members gm ON g.group_id = gm.group_id " +
                "WHERE gm.user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Group group = new Group();
                group.setGroupId(UUID.fromString(rs.getString("group_id")));
                group.setGroupName(rs.getString("group_name"));
                group.setCreatorId(UUID.fromString(rs.getString("creator_id")));
                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user groups: " + e.getMessage());
        }
        return groups;
    }
    // در DatabaseHelper.java این متدها رو اضافه کنید:
    public static boolean saveGroupMessage(GroupMessage message) {
        if (connection == null) return false;

        String sql = "INSERT INTO group_messages (message_id, group_id, sender_id, content, message_type) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessageId().toString());
            pstmt.setString(2, message.getGroupId().toString());
            pstmt.setString(3, message.getSenderId().toString());
            pstmt.setString(4, message.getContent());
            pstmt.setString(5, message.getType().toString());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving group message: " + e.getMessage());
            return false;
        }
    }

    public static List<UUID> getGroupMembers(UUID groupId) {
        List<UUID> members = new ArrayList<>();
        if (connection == null) return members;

        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, groupId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("user_id")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
        }
        return members;
    }

    public static List<GroupMessage> getGroupMessages(UUID groupId) {
        List<GroupMessage> messages = new ArrayList<>();
        if (connection == null) return messages;

        String sql = "SELECT message_id, sender_id, content, message_type, strftime('%H:%M', timestamp) as formatted_time " +
                "FROM group_messages WHERE group_id = ? ORDER BY timestamp";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, groupId.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                GroupMessage message = new GroupMessage();
                message.setMessageId(UUID.fromString(rs.getString("message_id")));
                message.setSenderId(UUID.fromString(rs.getString("sender_id")));
                message.setContent(rs.getString("content"));
                message.setType(GroupMessage.MessageType.valueOf(rs.getString("message_type")));
                message.setTimestamp(rs.getString("formatted_time"));
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting group messages: " + e.getMessage());
        }
        return messages;
    }
}