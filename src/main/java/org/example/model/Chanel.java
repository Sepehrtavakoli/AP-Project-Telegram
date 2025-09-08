package org.example.model;

import java.util.UUID;

public class Chanel {
    private UUID chanelID;
    private String chanelName;
    private UUID creatorID;

    public Chanel() {}

    public UUID getChanelID() {
        return chanelID;
    }
    public void setChanelID(UUID chanelID) {
        this.chanelID = chanelID;
    }
    public String getChanelName() {
        return chanelName;
    }
    public void setChanelName(String chanelName) {
        this.chanelName = chanelName;
    }
    public UUID getCreatorID() {
        return creatorID;
    }
    public void setCreatorID(UUID creatorID) {
        this.creatorID = creatorID;
    }
    @Override
    public String toString() {
        return chanelName;
    }
}
