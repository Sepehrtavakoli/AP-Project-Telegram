package org.example.API;

import org.example.model.User;

public class ClientManager {

    private static Client clientInstance;

    // متد برای گرفتن نمونه Client
    public static Client getInstance() {
        if (clientInstance == null) {
            clientInstance = new Client();
        }
        return clientInstance;
    }

    // متد برای برقراری اتصال
    public static boolean connect(User user) {
        if (clientInstance == null) {
            getInstance();
        }
        if (!clientInstance.isConnected()) {
            return clientInstance.connectToServer("localhost", 1234, user);
        }
        return true; // Already connected
    }

    // متد برای قطع اتصال
    public static void disconnect() {
        if (clientInstance != null && clientInstance.isConnected()) {
            clientInstance.closeEverything();
            clientInstance = null;
        }
    }
}