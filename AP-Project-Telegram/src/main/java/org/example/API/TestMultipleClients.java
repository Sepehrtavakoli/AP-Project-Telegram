package org.example.API;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestMultipleClients {

    public static void main(String[] args) {
        System.out.println("=== Testing Multiple Clients ===");

        System.out.println("Make sure Server is running first!");
        System.out.println("Press Enter when server is ready...");
        new Scanner(System.in).nextLine();

        testMultipleClients();
    }

    public static void testMultipleClients() {
        try {
            System.out.println("Creating Client 1...");
            Client client1 = new Client();
            boolean connected1 = client1.connectToServer("localhost", 1234, "Alice");
            System.out.println("Alice connected: " + connected1);

            Thread.sleep(1000);

            System.out.println("Creating Client 2...");
            Client client2 = new Client();
            boolean connected2 = client2.connectToServer("localhost", 1234, "Bob");
            System.out.println("Bob connected: " + connected2);

            Thread.sleep(1000);

            System.out.println("Creating Client 3...");
            Client client3 = new Client();
            boolean connected3 = client3.connectToServer("localhost", 1234, "Charlie");
            System.out.println("Charlie connected: " + connected3);

            if (connected1 && connected2 && connected3) {
                System.out.println("\n=== Testing Messages ===");

                Thread.sleep(2000);

                client1.sendMessage("Hello everyone! This is Alice");
                Thread.sleep(1000);

                client2.sendMessage("Hi Alice! Bob here");
                Thread.sleep(1000);

                client3.sendMessage("Hey guys! Charlie joining the conversation");
                Thread.sleep(1000);

                // Test private messages
                client1.sendMessage("/pm Bob Hey Bob, this is a private message!");
                Thread.sleep(1000);

                client2.sendMessage("/list"); // List connected users
                Thread.sleep(1000);

                System.out.println("\n=== Test completed. Check server console for results ===");
                System.out.println("Clients will remain connected. Press Enter to disconnect all...");
                new Scanner(System.in).nextLine();

                // Disconnect all clients
                client1.closeEverything();
                client2.closeEverything();
                client3.closeEverything();

            } else {
                System.out.println("Some clients failed to connect!");
            }

        } catch (InterruptedException e) {
            System.err.println("Test interrupted: " + e.getMessage());
        }
    }
}

// ServerTest.java - برای راه‌اندازی Server
class ServerTest {
    public static void main(String[] args) {
        System.out.println("Starting server for testing...");

        Server server = new Server();
        server.startServer();

        System.out.println("Server is running. You can now run TestMultipleClients");
        System.out.println("Press Enter to stop server...");
        new Scanner(System.in).nextLine();

        server.stopServer();
    }
}