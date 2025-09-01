package org.example.projectbackend;

import javafx.scene.image.Image;

public class Contact {
    private String PhoneNumber;
    private String FirstName;
    private String LastName;
    private Image Avatar;

    public void setUserData(String PhoneNumber, String FirstName, String LastName) {
        this.PhoneNumber = PhoneNumber;
        this.FirstName = FirstName;
        this.LastName = LastName;

        System.out.println(PhoneNumber + " " + FirstName + " " + LastName);
    }


}
