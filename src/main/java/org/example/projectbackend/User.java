package org.example.projectbackend;

import java.awt.*;

public class User {
    private int UserID;
    private String UserName;
    private Image profileImage;

    public User(int UserID, String UserName, Image profileImage) {
        this.UserID = UserID;
        this.UserName = UserName;
        this.profileImage = profileImage;
    }

    public int getUserID() {
        return UserID;
    }

    public String getUserName() {
        return UserName;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setUserID(int UserID) {

    }

    public void setUserName(String UserName) {

    }

    public void setProfileImage(Image profileImage) {

    }
}