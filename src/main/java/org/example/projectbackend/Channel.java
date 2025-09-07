package org.example.projectbackend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Channel {
    private UUID channelId;
    private String channelName;
    private UUID ownerId;
    private LocalDateTime createdAt;

    public Channel() {}

    public Channel(UUID channelId, String channelName, UUID ownerId, List<UUID> subscribers, LocalDateTime createdAt) {
    }

    public UUID getChannelId() { return channelId; }
    public void setChannelId(UUID channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "Channel{" + "id=" + channelId + ", name='" + channelName + '\'' + ", owner=" + ownerId + '}';
    }
}
