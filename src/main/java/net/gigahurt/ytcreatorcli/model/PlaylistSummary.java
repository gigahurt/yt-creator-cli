package net.gigahurt.ytcreatorcli.model;

public record PlaylistSummary(
    String playlistId,
    String title,
    String description,
    String visibility,
    int itemCount
) {}
