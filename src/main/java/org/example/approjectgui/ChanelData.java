package org.example.approjectgui;

import org.example.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChanelData {
    private static List<UUID> tempSelectedMembers;
    private static List<User> tempSelectedUserObjects;

    public static String chanelName;
    public static String chanelDescription;
    public static String avatarPath;

    public static List<UUID> getTempSelectedMembers() {
        return tempSelectedMembers;
    }

    public static void setTempSelectedMembers(List<UUID> tempSelectedMembers) {
        ChanelData.tempSelectedMembers = tempSelectedMembers;
    }

    public static List<User> getTempSelectedUserObjects() {
        return tempSelectedUserObjects;
    }

    public static void setTempSelectedUserObjects(List<User> tempSelectedUserObjects) {
        ChanelData.tempSelectedUserObjects = tempSelectedUserObjects;
    }

    public static void clearData() {
        tempSelectedMembers = null;
        tempSelectedUserObjects = null;
        chanelName = null;
        chanelDescription = null;
        avatarPath = null;
    }


}


