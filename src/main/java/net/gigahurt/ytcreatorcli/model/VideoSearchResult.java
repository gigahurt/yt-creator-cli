package net.gigahurt.ytcreatorcli.model;

public record VideoSearchResult(
    String videoId,
    String title,
    String description,
    String publishedAt
) {}
