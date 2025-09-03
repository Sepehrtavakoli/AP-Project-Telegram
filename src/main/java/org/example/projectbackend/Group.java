package org.example.projectbackend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// به Group.java اضافه کن:
public class Group {
    private UUID groupId;
    private String groupName;
    private UUID creatorId;
    private List<UUID> members;
    private LocalDateTime createdAt;

    // constructor, getters, setters
}
