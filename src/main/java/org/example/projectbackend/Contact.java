package org.example.projectbackend;

import org.example.model.User;

public class Contact {
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private User contactUser; // کاربر مرتبط با این مخاطب

    public Contact() {}

    public Contact(String phoneNumber, String firstName, String lastName) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getter و Setterها
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public User getContactUser() { return contactUser; }
    public void setContactUser(User contactUser) { this.contactUser = contactUser; }

    public String getFullName() {
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    public void setUserData(String phoneNumber, String firstName, String lastName) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        System.out.println("Contact created: " + phoneNumber + " " + firstName + " " + lastName);
    }
}