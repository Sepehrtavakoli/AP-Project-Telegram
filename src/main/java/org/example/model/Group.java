// کلاس Group در org.example.model
package org.example.model;

import java.util.UUID;

public class Group {
    private UUID groupId;
    private String groupName;
    private UUID creatorId;

    // Constructor, Getters, and Setters
    public Group() {}

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }

    @Override
    public String toString() {
        return groupName;
    }
}