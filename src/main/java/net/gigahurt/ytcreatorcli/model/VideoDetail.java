package net.gigahurt.ytcreatorcli.model;

import java.util.List;

public record VideoDetail(
    String videoId,
    String title,
    String description,
    List<String> tags,
    String categoryId,
    String defaultLanguage,
    String defaultAudioLanguage,
    String visibility,
    Boolean madeForKids,
    String publishAt,
    String publishedAt
) {}
