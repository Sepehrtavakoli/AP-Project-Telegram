package org.example.model;

import java.util.UUID;

public class User {
    private UUID userId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String avatarPath;

    // constructor بدون پارامتر (برای Gson)
    public User() {
        this.userId = UUID.randomUUID();
    }

    // constructor برای ثبت کاربر جدید
    public User(String firstName, String avatarPath, String phoneNumber) {
        this();
        this.firstName = firstName;
        this.avatarPath = avatarPath;
        this.phoneNumber = phoneNumber;
    }

    // constructor کامل با نام و فامیل
    public User(UUID userId, String firstName, String lastName, String phoneNumber) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    // constructor ساده‌تر (مثلاً برای نمایش در لیست)
    public User(UUID userId, String firstName, String phoneNumber) {
        this.userId = userId;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
    }

    // --- Getters & Setters ---
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    // 📌 نام کامل برای نمایش
    public String getUserName() {
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
