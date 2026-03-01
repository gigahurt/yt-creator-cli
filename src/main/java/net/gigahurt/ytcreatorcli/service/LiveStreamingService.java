package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastContentDetails;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamSnippet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class LiveStreamingService {

    private final YouTube youtube;

    public LiveStreamingService(YouTube youtube) {
        this.youtube = youtube;
    }

    public LiveBroadcast createBroadcast(String title, String description,
                                          String scheduledStartTime, String privacyStatus,
                                          boolean enableAutoStart, boolean enableAutoStop) throws IOException {
        LiveBroadcastSnippet snippet = new LiveBroadcastSnippet();
        snippet.setTitle(title);
        if (description != null && !description.isBlank()) {
            snippet.setDescription(description);
        }
        snippet.setScheduledStartTime(DateTime.parseRfc3339(scheduledStartTime));

        LiveBroadcastStatus status = new LiveBroadcastStatus();
        status.setPrivacyStatus(privacyStatus != null ? privacyStatus : "private");

        LiveBroadcastContentDetails contentDetails = new LiveBroadcastContentDetails();
        contentDetails.setEnableAutoStart(enableAutoStart);
        contentDetails.setEnableAutoStop(enableAutoStop);

        LiveBroadcast broadcast = new LiveBroadcast();
        broadcast.setSnippet(snippet);
        broadcast.setStatus(status);
        broadcast.setContentDetails(contentDetails);

        return youtube.liveBroadcasts()
                .insert(List.of("snippet", "status", "contentDetails"), broadcast)
                .execute();
    }

    public List<LiveBroadcast> listBroadcasts(String broadcastStatus, long maxResults) throws IOException {
        YouTube.LiveBroadcasts.List request = youtube.liveBroadcasts()
                .list(List.of("snippet", "status", "contentDetails"))
                .setBroadcastStatus(broadcastStatus != null ? broadcastStatus : "upcoming")
                .setMaxResults(maxResults);
        var response = request.execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public LiveBroadcast updateBroadcast(String broadcastId, String title, String description,
                                          String scheduledStartTime, String privacyStatus) throws IOException {
        YouTube.LiveBroadcasts.List getRequest = youtube.liveBroadcasts()
                .list(List.of("snippet", "status"))
                .setId(List.of(broadcastId));
        var response = getRequest.execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Broadcast not found: " + broadcastId);
        }
        LiveBroadcast broadcast = response.getItems().getFirst();

        if (title != null && !title.isBlank()) {
            broadcast.getSnippet().setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            broadcast.getSnippet().setDescription(description);
        }
        if (scheduledStartTime != null && !scheduledStartTime.isBlank()) {
            broadcast.getSnippet().setScheduledStartTime(DateTime.parseRfc3339(scheduledStartTime));
        }
        if (privacyStatus != null && !privacyStatus.isBlank()) {
            broadcast.getStatus().setPrivacyStatus(privacyStatus);
        }

        return youtube.liveBroadcasts()
                .update(List.of("snippet", "status"), broadcast)
                .execute();
    }

    public void deleteBroadcast(String broadcastId) throws IOException {
        youtube.liveBroadcasts().delete(broadcastId).execute();
    }

    public LiveBroadcast bindBroadcast(String broadcastId, String streamId) throws IOException {
        return youtube.liveBroadcasts()
                .bind(broadcastId, List.of("id", "snippet"))
                .setStreamId(streamId)
                .execute();
    }

    public LiveBroadcast transitionBroadcast(String broadcastId, String broadcastStatus) throws IOException {
        return youtube.liveBroadcasts()
                .transition(broadcastStatus, broadcastId, List.of("id", "status"))
                .execute();
    }

    public LiveStream createStream(String title, String frameRate, String resolution,
                                    String ingestionType) throws IOException {
        LiveStreamSnippet snippet = new LiveStreamSnippet();
        snippet.setTitle(title);

        CdnSettings cdn = new CdnSettings();
        cdn.setFrameRate(frameRate != null ? frameRate : "variable");
        cdn.setResolution(resolution != null ? resolution : "variable");
        cdn.setIngestionType(ingestionType != null ? ingestionType : "rtmp");

        LiveStream stream = new LiveStream();
        stream.setSnippet(snippet);
        stream.setCdn(cdn);

        return youtube.liveStreams()
                .insert(List.of("snippet", "cdn"), stream)
                .execute();
    }

    public List<LiveStream> listStreams(long maxResults) throws IOException {
        var response = youtube.liveStreams()
                .list(List.of("snippet", "cdn", "status"))
                .setMine(true)
                .setMaxResults(maxResults)
                .execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public LiveStream updateStream(String streamId, String title) throws IOException {
        YouTube.LiveStreams.List getRequest = youtube.liveStreams()
                .list(List.of("snippet", "cdn"))
                .setId(List.of(streamId));
        var response = getRequest.execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Stream not found: " + streamId);
        }
        LiveStream stream = response.getItems().getFirst();
        stream.getSnippet().setTitle(title);

        return youtube.liveStreams()
                .update(List.of("snippet"), stream)
                .execute();
    }

    public void deleteStream(String streamId) throws IOException {
        youtube.liveStreams().delete(streamId).execute();
    }
}
