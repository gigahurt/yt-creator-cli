package net.gigahurt.ytcreatorcli.model;

public record LiveBroadcastDetail(
    String broadcastId,
    String title,
    String description,
    String scheduledStartTime,
    String actualStartTime,
    String liveChatId,
    String lifeCycleStatus,
    String privacyStatus,
    String boundStreamId
) {}
