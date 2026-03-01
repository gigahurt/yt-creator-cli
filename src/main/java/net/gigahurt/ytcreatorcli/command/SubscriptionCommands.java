package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;

import net.gigahurt.ytcreatorcli.service.SubscriptionService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionCommands {

    private final SubscriptionService subscriptionService;

    public SubscriptionCommands(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Command(name = "list-subscriptions", description = "List your channel subscriptions")
    public String listSubscriptions(
            @Option(longName = "max-results", description = "Max results", defaultValue = "25") Long maxResults,
            @Option(longName = "order", description = "alphabetical, relevance, or unread", defaultValue = "alphabetical") String order,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            SubscriptionListResponse response = subscriptionService.listMySubscriptions(
                    maxResults, (pageToken == null || pageToken.isEmpty()) ? null : pageToken, order);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No subscriptions found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Subscriptions (%d):\n", response.getItems().size()));
            for (Subscription s : response.getItems()) {
                SubscriptionSnippet snippet = s.getSnippet();
                int newItems = (s.getContentDetails() != null && s.getContentDetails().getNewItemCount() != null)
                        ? s.getContentDetails().getNewItemCount().intValue() : 0;
                sb.append(String.format("  ID: %s  Channel: %s  Title: %s  New videos: %d\n",
                        s.getId(),
                        snippet != null && snippet.getResourceId() != null
                                ? snippet.getResourceId().getChannelId() : "",
                        snippet != null ? snippet.getTitle() : "",
                        newItems));
            }
            if (response.getNextPageToken() != null) {
                sb.append("---\nNext page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "subscribe", description = "Subscribe to a YouTube channel")
    public String subscribe(
            @Option(longName = "channel-id", description = "Target channel ID", required = true) String channelId) {
        try {
            Subscription sub = subscriptionService.subscribe(channelId);
            return String.format("""
                    Subscribed to %s
                      Subscription ID: %s
                    """,
                    channelId,
                    sub.getId());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "unsubscribe", description = "Unsubscribe from a channel by subscription ID (use list-subscriptions or check-subscription to find it)")
    public String unsubscribe(
            @Option(longName = "subscription-id", description = "Subscription ID (from list-subscriptions)", required = true) String subscriptionId) {
        try {
            subscriptionService.unsubscribe(subscriptionId);
            return "Unsubscribed: " + subscriptionId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "check-subscription", description = "Check if you are subscribed to a channel")
    public String checkSubscription(
            @Option(longName = "channel-id", description = "Channel ID to check", required = true) String channelId) {
        try {
            String subscriptionId = subscriptionService.findSubscription(channelId);
            if (subscriptionId != null) {
                return String.format("""
                        Subscribed to %s
                          Subscription ID: %s
                        """,
                        channelId, subscriptionId);
            } else {
                return "Not subscribed to " + channelId;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
