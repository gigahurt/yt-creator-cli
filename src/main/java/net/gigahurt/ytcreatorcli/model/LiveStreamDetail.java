package net.gigahurt.ytcreatorcli.model;

public record LiveStreamDetail(
    String streamId,
    String title,
    String ingestionType,
    String ingestionAddress,
    String streamName,
    String frameRate,
    String resolution,
    String streamStatus
) {}
