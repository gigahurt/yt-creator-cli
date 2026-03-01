package net.gigahurt.ytcreatorcli.model;

public record CommentSummary(
    String threadId,
    String commentId,
    String authorName,
    String authorChannelId,
    String text,
    long likeCount,
    long replyCount,
    String publishedAt,
    String moderationStatus
) {}
