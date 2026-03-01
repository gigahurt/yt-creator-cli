package net.gigahurt.ytcreatorcli.model;

public record SubscriptionSummary(
    String subscriptionId,
    String channelId,
    String channelTitle,
    int newItemCount,
    String subscribedAt
) {}
