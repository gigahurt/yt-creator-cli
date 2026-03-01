package net.gigahurt.ytcreatorcli.model;

public record PlaylistItemSummary(
    String itemId,
    String videoId,
    String title,
    long position
) {}
