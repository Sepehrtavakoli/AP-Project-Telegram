package org.example.projectbackend;

import org.example.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private UUID groupId;
    private String groupName;
    private UUID creatorId;
    private List<User> members;  // 👈 تغییر دادیم به User
    private LocalDateTime createdAt;

    // --- Constructors ---
    public Group(UUID groupId, String groupName, UUID creatorId, List<User> members, LocalDateTime createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.creatorId = creatorId;
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // سازنده برای وقتی فقط اسم و creator معلومه
    public Group(String groupName, UUID creatorId) {
        this(UUID.randomUUID(), groupName, creatorId, new ArrayList<>(), LocalDateTime.now());
        // سازنده رو به عنوان اولین عضو اضافه کن (User باید از دیتابیس گرفته بشه)
    }

    // --- Getters & Setters ---
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- Utility ---
    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", creatorId=" + creatorId +
                ", members=" + members +
                ", createdAt=" + createdAt +
                '}';
    }
}
