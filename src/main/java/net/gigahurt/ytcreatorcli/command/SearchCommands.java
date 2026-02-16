package net.gigahurt.ytcreatorcli.command;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import net.gigahurt.ytcreatorcli.model.VideoSearchResult;
import net.gigahurt.ytcreatorcli.service.YouTubeService;

import java.util.List;

@Component
public class SearchCommands {

    private final YouTubeService youTubeService;

    public SearchCommands(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @Command(name = "search-videos", description = "Search videos of a specific YouTube channel")
    public String searchVideos(
            @Option(longName = "channel-id", description = "YouTube channel ID", required = true) String channelId,
            @Option(longName = "query", description = "Optional search query", defaultValue = "") String query,
            @Option(longName = "max-results", description = "Max results (1-50)", defaultValue = "10") Long maxResults) {
        try {
            List<VideoSearchResult> results = youTubeService.searchVideos(channelId, query, maxResults);

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
            @Option(longName = "channel-id", description = "YouTube channel ID", required = true) String channelId,
            @Option(longName = "query", description = "Optional filter by title/description", defaultValue = "") String query,
            @Option(longName = "max-results", description = "Max results", defaultValue = "50") Long maxResults) {
        try {
            var results = youTubeService.listChannelVideos(channelId, query, maxResults);

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

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
