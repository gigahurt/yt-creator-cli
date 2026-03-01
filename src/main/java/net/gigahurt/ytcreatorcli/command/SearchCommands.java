package net.gigahurt.ytcreatorcli.command;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import net.gigahurt.ytcreatorcli.config.AppProperties;
import net.gigahurt.ytcreatorcli.model.VideoSearchResult;
import net.gigahurt.ytcreatorcli.service.YouTubeService;

import java.util.List;

@Component
public class SearchCommands {

    private final YouTubeService youTubeService;
    private final AppProperties props;

    public SearchCommands(YouTubeService youTubeService, AppProperties props) {
        this.youTubeService = youTubeService;
        this.props = props;
    }

    @Command(name = "search-videos", description = "Search videos of a specific YouTube channel")
    public String searchVideos(
            @Option(longName = "channel-id", description = "YouTube channel ID (or set YTCLI_CHANNEL_ID)") String channelId,
            @Option(longName = "query", description = "Optional search query") String query,
            @Option(longName = "max-results", description = "Max results (1-50)", defaultValue = "10") Long maxResults) {
        try {
            String resolvedChannelId = resolveChannelId(channelId);
            List<VideoSearchResult> results = youTubeService.searchVideos(resolvedChannelId, query, maxResults);

            if (results.isEmpty()) {
                return "No videos found.";
            }

            StringBuilder sb = new StringBuilder();
            for (VideoSearchResult r : results) {
                sb.append("ID: ").append(r.videoId()).append("\n");
                sb.append("Title: ").append(r.title()).append("\n");
                sb.append("Published: ").append(r.publishedAt()).append("\n");
                sb.append("Description: ").append(truncate(r.description(), 120)).append("\n");
                sb.append("---\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-videos", description = "List all videos on a channel (including private/unlisted)")
    public String listVideos(
            @Option(longName = "channel-id", description = "YouTube channel ID (or set YTCLI_CHANNEL_ID)") String channelId,
            @Option(longName = "query", description = "Optional filter by title/description") String query,
            @Option(longName = "max-results", description = "Max results", defaultValue = "50") Long maxResults) {
        try {
            String resolvedChannelId = resolveChannelId(channelId);
            var results = youTubeService.listChannelVideos(resolvedChannelId, query, maxResults);

            if (results.isEmpty()) {
                return "No videos found.";
            }

            StringBuilder sb = new StringBuilder();
            for (var r : results) {
                sb.append("ID: ").append(r.videoId()).append("\n");
                sb.append("Title: ").append(r.title()).append("\n");
                sb.append("Published: ").append(r.publishedAt()).append("\n");
                sb.append("Description: ").append(truncate(r.description(), 120)).append("\n");
                sb.append("---\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String resolveChannelId(String channelId) {
        if (channelId != null && !channelId.isEmpty()) {
            return channelId;
        }
        if (props.channelId() != null && !props.channelId().isEmpty()) {
            return props.channelId();
        }
        throw new IllegalArgumentException("No channel ID provided. Use --channel-id or set YTCLI_CHANNEL_ID");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
