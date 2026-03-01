package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;

import net.gigahurt.ytcreatorcli.config.AppProperties;
import net.gigahurt.ytcreatorcli.service.PlaylistService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class PlaylistCommands {

    private final PlaylistService playlistService;
    private final AppProperties props;

    public PlaylistCommands(PlaylistService playlistService, AppProperties props) {
        this.playlistService = playlistService;
        this.props = props;
    }

    private String resolveChannelId(String channelId) {
        if (channelId != null && !channelId.isEmpty()) {
            return channelId;
        }
        if (props.channelId() != null && !props.channelId().isEmpty()) {
            return props.channelId();
        }
        return null; // service will use mine=true
    }

    @Command(name = "create-playlist", description = "Create a new YouTube playlist")
    public String createPlaylist(
            @Option(longName = "title", description = "Playlist title", required = true) String title,
            @Option(longName = "description", description = "Playlist description") String description,
            @Option(longName = "visibility", description = "public, private, or unlisted", defaultValue = "private") String visibility) {
        try {
            Playlist playlist = playlistService.createPlaylist(title, description, visibility);
            return String.format("""
                    Playlist created:
                      ID: %s
                      Title: %s
                      URL: https://www.youtube.com/playlist?list=%s
                    """,
                    playlist.getId(),
                    playlist.getSnippet().getTitle(),
                    playlist.getId());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "add-to-playlist", description = "Add a video to a playlist")
    public String addToPlaylist(
            @Option(longName = "playlist-id", description = "Target playlist ID", required = true) String playlistId,
            @Option(longName = "video-id", description = "Video to add", required = true) String videoId,
            @Option(longName = "position", description = "0-based position in playlist", defaultValue = "-1") Long position) {
        try {
            Long effPosition = (position != null && position >= 0) ? position : null;
            PlaylistItem item = playlistService.addToPlaylist(playlistId, videoId, effPosition);
            return String.format("""
                    Added to playlist:
                      Playlist: %s
                      Video: %s
                      Position: %d
                    """,
                    playlistId,
                    videoId,
                    item.getSnippet().getPosition());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-playlists", description = "List playlists on a channel")
    public String listPlaylists(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID; omit for own channel)") String channelId,
            @Option(longName = "max-results", description = "Max results", defaultValue = "25") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            String resolvedChannelId = resolveChannelId(channelId);
            PlaylistListResponse response = playlistService.listPlaylists(resolvedChannelId, maxResults, pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No playlists found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Playlists (%d):\n", response.getItems().size()));
            for (var pl : response.getItems()) {
                var snippet = pl.getSnippet();
                var status = pl.getStatus();
                int count = pl.getContentDetails() != null ? pl.getContentDetails().getItemCount().intValue() : 0;
                sb.append(String.format("  ID: %s  Title: %s  Visibility: %s  Videos: %d\n",
                        pl.getId(),
                        snippet != null ? snippet.getTitle() : "",
                        status != null ? status.getPrivacyStatus() : "",
                        count));
                sb.append(String.format("    URL: https://www.youtube.com/playlist?list=%s\n", pl.getId()));
            }
            if (response.getNextPageToken() != null) {
                sb.append("---\nNext page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-playlist", description = "Update playlist title, description, or visibility")
    public String updatePlaylist(
            @Option(longName = "playlist-id", description = "Playlist ID", required = true) String playlistId,
            @Option(longName = "title", description = "New title") String title,
            @Option(longName = "description", description = "New description") String description,
            @Option(longName = "visibility", description = "public, private, or unlisted") String visibility) {
        try {
            String effTitle = (title == null || title.isEmpty()) ? null : title;
            String effDescription = (description == null || description.isEmpty()) ? null : description;
            String effVisibility = (visibility == null || visibility.isEmpty()) ? null : visibility;
            Playlist updated = playlistService.updatePlaylist(playlistId, effTitle, effDescription, effVisibility);
            return String.format("""
                    Playlist updated:
                      ID: %s
                      Title: %s
                      Visibility: %s
                    """,
                    updated.getId(),
                    updated.getSnippet().getTitle(),
                    updated.getStatus().getPrivacyStatus());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-playlist", description = "Delete a playlist permanently")
    public String deletePlaylist(
            @Option(longName = "playlist-id", description = "Playlist ID", required = true) String playlistId) {
        try {
            playlistService.deletePlaylist(playlistId);
            return "Deleted playlist: " + playlistId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-playlist-items", description = "List videos in a playlist")
    public String listPlaylistItems(
            @Option(longName = "playlist-id", description = "Playlist ID", required = true) String playlistId,
            @Option(longName = "max-results", description = "Max results", defaultValue = "25") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            PlaylistItemListResponse response = playlistService.listPlaylistItems(playlistId, maxResults, pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No items found in playlist: " + playlistId;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Items in %s (%d):\n", playlistId, response.getItems().size()));
            for (var item : response.getItems()) {
                var snippet = item.getSnippet();
                long pos = snippet != null && snippet.getPosition() != null ? snippet.getPosition() : 0;
                String videoId = snippet != null && snippet.getResourceId() != null
                        ? snippet.getResourceId().getVideoId() : "";
                String title = snippet != null ? snippet.getTitle() : "";
                sb.append(String.format("  #%d  ItemID: %s  VideoID: %s  Title: %s\n",
                        pos + 1, item.getId(), videoId, title));
            }
            if (response.getNextPageToken() != null) {
                sb.append("---\nNext page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-playlist-item", description = "Move a playlist item to a new position")
    public String updatePlaylistItem(
            @Option(longName = "item-id", description = "PlaylistItem ID", required = true) String itemId,
            @Option(longName = "playlist-id", description = "Playlist ID", required = true) String playlistId,
            @Option(longName = "video-id", description = "Video ID", required = true) String videoId,
            @Option(longName = "position", description = "New 0-based position", required = true) Long position) {
        try {
            PlaylistItem updated = playlistService.updatePlaylistItem(itemId, playlistId, videoId, position);
            return String.format("""
                    Playlist item updated:
                      Item ID: %s
                      Video: %s
                      Position: %d
                    """,
                    updated.getId(),
                    videoId,
                    updated.getSnippet().getPosition());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "remove-playlist-item", description = "Remove a video from a playlist")
    public String removePlaylistItem(
            @Option(longName = "item-id", description = "PlaylistItem ID", required = true) String itemId) {
        try {
            playlistService.deletePlaylistItem(itemId);
            return "Removed playlist item: " + itemId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
