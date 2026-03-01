package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SubscriptionService {

    private final YouTube youtube;

    public SubscriptionService(YouTube youtube) {
        this.youtube = youtube;
    }

    public SubscriptionListResponse listMySubscriptions(long maxResults, String pageToken,
                                                         String order) throws IOException {
        YouTube.Subscriptions.List request = youtube.subscriptions()
                .list(List.of("snippet", "contentDetails"))
                .setMine(true)
                .setMaxResults(maxResults)
                .setOrder(order != null ? order : "alphabetical");
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public Subscription subscribe(String channelId) throws IOException {
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#channel");
        resourceId.setChannelId(channelId);

        SubscriptionSnippet snippet = new SubscriptionSnippet();
        snippet.setResourceId(resourceId);

        Subscription subscription = new Subscription();
        subscription.setSnippet(snippet);

        return youtube.subscriptions()
                .insert(List.of("snippet"), subscription)
                .execute();
    }

    public void unsubscribe(String subscriptionId) throws IOException {
        youtube.subscriptions().delete(subscriptionId).execute();
    }

    public String findSubscription(String channelId) throws IOException {
        SubscriptionListResponse response = youtube.subscriptions()
                .list(List.of("snippet"))
                .setMine(true)
                .setForChannelId(channelId)
                .execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return null;
        }
        return response.getItems().getFirst().getId();
    }
}
