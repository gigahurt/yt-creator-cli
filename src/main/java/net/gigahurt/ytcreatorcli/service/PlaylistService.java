package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PlaylistService {

    private final YouTube youtube;

    public PlaylistService(YouTube youtube) {
        this.youtube = youtube;
    }

    public Playlist createPlaylist(String title, String description,
                                    String visibility) throws IOException {
        PlaylistSnippet snippet = new PlaylistSnippet();
        snippet.setTitle(title);
        if (description != null && !description.isBlank()) {
            snippet.setDescription(description);
        }

        PlaylistStatus status = new PlaylistStatus();
        status.setPrivacyStatus(visibility != null ? visibility : "private");

        Playlist playlist = new Playlist();
        playlist.setSnippet(snippet);
        playlist.setStatus(status);

        return youtube.playlists()
                .insert(List.of("snippet", "status"), playlist)
                .execute();
    }

    public PlaylistItem addToPlaylist(String playlistId, String videoId,
                                       Long position) throws IOException {
        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setPlaylistId(playlistId);

        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);
        snippet.setResourceId(resourceId);

        if (position != null) {
            snippet.setPosition(position);
        }

        PlaylistItem item = new PlaylistItem();
        item.setSnippet(snippet);

        return youtube.playlistItems()
                .insert(List.of("snippet"), item)
                .execute();
    }
}
