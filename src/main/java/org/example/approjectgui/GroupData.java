package org.example.approjectgui;

import org.example.model.User;
import java.util.List;
import java.util.UUID;

public class GroupData {
    private static List<UUID> tempSelectedMembers;
    private static List<User> tempSelectedUserObjects;

    public static List<UUID> getTempSelectedMembers() {
        return tempSelectedMembers;
    }

    public static void setTempSelectedMembers(List<UUID> tempSelectedMembers) {
        GroupData.tempSelectedMembers = tempSelectedMembers;
    }

    public static List<User> getTempSelectedUserObjects() {
        return tempSelectedUserObjects;
    }

    public static void setTempSelectedUserObjects(List<User> tempSelectedUserObjects) {
        GroupData.tempSelectedUserObjects = tempSelectedUserObjects;
    }

    public static void clearData() {
        tempSelectedMembers = null;
        tempSelectedUserObjects = null;
    }
}