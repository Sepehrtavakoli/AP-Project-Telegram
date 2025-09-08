💬 Chat Application
A modern, multi-functional chat application with private, group, and channel messaging capabilities.

https://img.shields.io/badge/Java-17%252B-blue
https://img.shields.io/badge/JavaFX-19-orange
https://img.shields.io/badge/SQLite-Database-lightgrey
https://img.shields.io/badge/License-MIT-green

✨ Key Features
💬 Private Messaging - Direct communication between users

👥 Group Chats - Create and manage group conversations

📢 Channels - Public channels with creator-only posting

🖼️ Image Sharing - Send and receive images in conversations

🌐 Real-time Networking - Instant messaging via dedicated server

🔐 User Accounts - Registration, login, and profile management

🎨 Custom Avatars - Personalize your profile with custom images

✏️ Message Management - Edit and delete your messages

🏗️ System Architecture
text
Client (JavaFX) ↔ Server (Java Socket) ↔ Database (SQLite)
📦 Installation & Setup
Prerequisites
Java JDK 17 or higher

JavaFX 19 or higher

SQLite (embedded)

Quick Start
Clone the Repository

bash
git clone <repository-url>
cd chat-application
Build the Project

bash
# Compile the project
javac --module-path path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -d out src/**/*.java
Run the Application

bash
# Start the server first
java -cp out org.example.API.Server

# Run the client application
java --module-path path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp out org.example.approjectgui.Main
🚀 Usage Guide
Creating an Account
Launch the application

Click "Sign Up"

Fill in your details

Upload a profile picture (optional)

Complete registration

Starting a Chat
Private Chat: Select a user from contacts list

Group Chat: Create a group and add members

Channel: Join existing channels or create your own

Sending Messages
Type your message in the input field

Press Enter or click Send

Use the attachment button to send images

Right-click messages to edit or delete

🗂️ Project Structure
text
src/
├── org/example/
│   ├── approjectgui/          # JavaFX GUI controllers
│   ├── API/                   # Client-Server communication
│   ├── database/              # Database operations
│   ├── model/                 # Data models (User, Message, etc.)
│   └── projectbackend/        # Core business logic
🔧 Configuration
Database
The application uses SQLite with automatic database creation:

Database file: chat_app.db

Auto-generated on first run

No manual configuration needed

Server Configuration
Default port: 1234

Host: localhost

Configurable in ClientManager.java

🎨 UI Overview
Home Page: Chat list and navigation menu

Chat Interface: Conversation view with message bubbles

Contacts Page: User management and search

Settings Page: Profile customization

Channel Management: Create and manage channels

📊 Message Types
TEXT - Regular text messages

IMAGE - Base64 encoded images

SYSTEM - Server notifications

EDIT - Message edits

DELETE - Message deletions

🔄 Real-time Features
Instant message delivery

Online/offline status indicators

Typing indicators (planned)

Message read receipts (planned)

🛠️ Development
Adding New Features
Extend appropriate model class

Update database schema in DatabaseHelper

Modify server-side handling in ClientHandler

Update client-side UI and controllers

Database Schema Updates
Modify DatabaseHelper.initializeDatabase() to include new tables or columns.

🤝 Contributing
Fork the repository

Create a feature branch

Make your changes

Test thoroughly

Submit a pull request

📝 License
This project is licensed under the MIT License - see the LICENSE file for details.

🆘 Support
For issues and questions:

Check existing GitHub issues

Create a new issue with detailed description

Include error logs and reproduction steps

🚀 Future Enhancements
Voice messages support

Video calling

Message encryption

File sharing

Theme customization

Mobile app version

Enjoy connecting with your friends and colleagues! 🎉