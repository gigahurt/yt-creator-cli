package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamSnippet;

import net.gigahurt.ytcreatorcli.service.LiveStreamingService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LiveCommands {

    private final LiveStreamingService liveStreamingService;

    public LiveCommands(LiveStreamingService liveStreamingService) {
        this.liveStreamingService = liveStreamingService;
    }

    @Command(name = "list-broadcasts", description = "List live broadcasts")
    public String listBroadcasts(
            @Option(longName = "status", description = "upcoming, active, or completed", defaultValue = "upcoming") String status,
            @Option(longName = "max-results", description = "Max results", defaultValue = "10") Long maxResults) {
        try {
            List<LiveBroadcast> broadcasts = liveStreamingService.listBroadcasts(status, maxResults);
            if (broadcasts.isEmpty()) {
                return "No " + status + " broadcasts found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Broadcasts (%s, %d):\n", status, broadcasts.size()));
            for (LiveBroadcast b : broadcasts) {
                LiveBroadcastSnippet s = b.getSnippet();
                String liveStatus = b.getStatus() != null ? b.getStatus().getLifeCycleStatus() : "";
                String startTime = s != null && s.getScheduledStartTime() != null
                        ? s.getScheduledStartTime().toString() : "";
                sb.append(String.format("  ID: %s  Title: %s  Status: %s  Start: %s\n",
                        b.getId(),
                        s != null ? s.getTitle() : "",
                        liveStatus,
                        startTime));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "create-broadcast", description = "Create a live broadcast")
    public String createBroadcast(
            @Option(longName = "title", description = "Broadcast title", required = true) String title,
            @Option(longName = "description", description = "Description") String description,
            @Option(longName = "start-time", description = "Scheduled start time (ISO 8601, e.g. 2026-04-01T18:00:00Z)", required = true) String startTime,
            @Option(longName = "visibility", description = "public, private, or unlisted", defaultValue = "private") String visibility,
            @Option(longName = "auto-start", description = "Enable auto-start (true/false)", defaultValue = "false") String autoStart,
            @Option(longName = "auto-stop", description = "Enable auto-stop (true/false)", defaultValue = "false") String autoStop) {
        try {
            LiveBroadcast broadcast = liveStreamingService.createBroadcast(
                    title,
                    (description == null || description.isEmpty()) ? null : description,
                    startTime,
                    visibility,
                    Boolean.parseBoolean(autoStart),
                    Boolean.parseBoolean(autoStop));
            LiveBroadcastSnippet s = broadcast.getSnippet();
            return String.format("""
                    Broadcast created:
                      ID: %s
                      Title: %s
                      Scheduled start: %s
                      Visibility: %s
                      Status: %s
                    """,
                    broadcast.getId(),
                    s != null ? s.getTitle() : "",
                    s != null && s.getScheduledStartTime() != null ? s.getScheduledStartTime() : "",
                    broadcast.getStatus() != null ? broadcast.getStatus().getPrivacyStatus() : "",
                    broadcast.getStatus() != null ? broadcast.getStatus().getLifeCycleStatus() : "");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-broadcast", description = "Update a live broadcast")
    public String updateBroadcast(
            @Option(longName = "broadcast-id", description = "Broadcast ID", required = true) String broadcastId,
            @Option(longName = "title", description = "New title") String title,
            @Option(longName = "description", description = "New description") String description,
            @Option(longName = "start-time", description = "New start time (ISO 8601)") String startTime,
            @Option(longName = "visibility", description = "public, private, or unlisted") String visibility) {
        try {
            LiveBroadcast updated = liveStreamingService.updateBroadcast(
                    broadcastId,
                    (title == null || title.isEmpty()) ? null : title,
                    (description == null || description.isEmpty()) ? null : description,
                    (startTime == null || startTime.isEmpty()) ? null : startTime,
                    (visibility == null || visibility.isEmpty()) ? null : visibility);
            return String.format("Broadcast updated: %s  Title: %s",
                    updated.getId(),
                    updated.getSnippet() != null ? updated.getSnippet().getTitle() : "");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-broadcast", description = "Delete a live broadcast")
    public String deleteBroadcast(
            @Option(longName = "broadcast-id", description = "Broadcast ID", required = true) String broadcastId) {
        try {
            liveStreamingService.deleteBroadcast(broadcastId);
            return "Deleted broadcast: " + broadcastId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "bind-broadcast", description = "Bind a broadcast to a live stream ingestion point")
    public String bindBroadcast(
            @Option(longName = "broadcast-id", description = "Broadcast ID", required = true) String broadcastId,
            @Option(longName = "stream-id", description = "Live stream ID", required = true) String streamId) {
        try {
            LiveBroadcast bound = liveStreamingService.bindBroadcast(broadcastId, streamId);
            return String.format("Broadcast %s bound to stream %s", bound.getId(), streamId);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "transition-broadcast", description = "Transition broadcast status (testing/live/complete)")
    public String transitionBroadcast(
            @Option(longName = "broadcast-id", description = "Broadcast ID", required = true) String broadcastId,
            @Option(longName = "status", description = "testing, live, or complete", required = true) String status) {
        try {
            LiveBroadcast transitioned = liveStreamingService.transitionBroadcast(broadcastId, status);
            String lifeCycle = transitioned.getStatus() != null
                    ? transitioned.getStatus().getLifeCycleStatus() : status;
            return String.format("Broadcast %s transitioned to: %s", broadcastId, lifeCycle);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-streams", description = "List live stream ingestion points")
    public String listStreams(
            @Option(longName = "max-results", description = "Max results", defaultValue = "10") Long maxResults) {
        try {
            List<LiveStream> streams = liveStreamingService.listStreams(maxResults);
            if (streams.isEmpty()) {
                return "No live streams found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Streams (%d):\n", streams.size()));
            for (LiveStream s : streams) {
                LiveStreamSnippet snippet = s.getSnippet();
                CdnSettings cdn = s.getCdn();
                String streamStatus = s.getStatus() != null ? s.getStatus().getStreamStatus() : "";
                String ingestionAddress = cdn != null && cdn.getIngestionInfo() != null
                        ? cdn.getIngestionInfo().getIngestionAddress() : "";
                sb.append(String.format("  ID: %s  Title: %s  Type: %s  Status: %s\n",
                        s.getId(),
                        snippet != null ? snippet.getTitle() : "",
                        cdn != null ? cdn.getIngestionType() : "",
                        streamStatus));
                if (!ingestionAddress.isEmpty()) {
                    sb.append(String.format("  Ingestion: %s\n", ingestionAddress));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "create-stream", description = "Create a live stream ingestion point")
    public String createStream(
            @Option(longName = "title", description = "Stream title", required = true) String title,
            @Option(longName = "frame-rate", description = "30fps, 60fps, or variable", defaultValue = "variable") String frameRate,
            @Option(longName = "resolution", description = "1080p, 720p, 480p, 360p, 240p, or variable", defaultValue = "variable") String resolution,
            @Option(longName = "ingestion-type", description = "rtmp, dash, or webrtc", defaultValue = "rtmp") String ingestionType) {
        try {
            LiveStream stream = liveStreamingService.createStream(title, frameRate, resolution, ingestionType);
            CdnSettings cdn = stream.getCdn();
            String ingestionAddress = cdn != null && cdn.getIngestionInfo() != null
                    ? cdn.getIngestionInfo().getIngestionAddress() : "";
            String streamKey = cdn != null && cdn.getIngestionInfo() != null
                    ? cdn.getIngestionInfo().getStreamName() : "";
            return String.format("""
                    Stream created:
                      ID: %s
                      Title: %s
                      Ingestion type: %s
                      Ingestion address: %s
                      Stream key: %s
                    """,
                    stream.getId(),
                    stream.getSnippet() != null ? stream.getSnippet().getTitle() : "",
                    cdn != null ? cdn.getIngestionType() : "",
                    ingestionAddress,
                    streamKey);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-stream", description = "Update a live stream title")
    public String updateStream(
            @Option(longName = "stream-id", description = "Stream ID", required = true) String streamId,
            @Option(longName = "title", description = "New title", required = true) String title) {
        try {
            LiveStream updated = liveStreamingService.updateStream(streamId, title);
            return String.format("Stream updated: %s  Title: %s",
                    updated.getId(),
                    updated.getSnippet() != null ? updated.getSnippet().getTitle() : "");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-stream", description = "Delete a live stream")
    public String deleteStream(
            @Option(longName = "stream-id", description = "Stream ID", required = true) String streamId) {
        try {
            liveStreamingService.deleteStream(streamId);
            return "Deleted stream: " + streamId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
