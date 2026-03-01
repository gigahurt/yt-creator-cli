package net.gigahurt.ytcreatorcli.model;

public record ChannelDetail(
    String channelId,
    String title,
    String description,
    String country,
    String defaultLanguage,
    String customUrl,
    long subscriberCount,
    long viewCount,
    long videoCount,
    String publishedAt
) {}
