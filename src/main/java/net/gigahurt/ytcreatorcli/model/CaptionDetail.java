package net.gigahurt.ytcreatorcli.model;

public record CaptionDetail(
    String captionId,
    String language,
    String name,
    String trackKind,
    String status,
    boolean isDraft,
    String lastUpdated
) {}
