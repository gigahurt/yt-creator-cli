package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;

import net.gigahurt.ytcreatorcli.service.PlaylistService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class PlaylistCommands {

    private final PlaylistService playlistService;

    public PlaylistCommands(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @Command(name = "create-playlist", description = "Create a new YouTube playlist")
    public String createPlaylist(
            @Option(longName = "title", description = "Playlist title", required = true) String title,
            @Option(longName = "description", description = "Playlist description", defaultValue = "") String description,
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
}
