package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
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

    public PlaylistListResponse listPlaylists(String channelId, long maxResults,
                                               String pageToken) throws IOException {
        YouTube.Playlists.List request = youtube.playlists()
                .list(List.of("snippet", "status", "contentDetails"));
        if (channelId != null && !channelId.isBlank()) {
            request.setChannelId(channelId);
        } else {
            request.setMine(true);
        }
        request.setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public Playlist updatePlaylist(String playlistId, String title, String description,
                                    String visibility) throws IOException {
        YouTube.Playlists.List getRequest = youtube.playlists()
                .list(List.of("snippet", "status"))
                .setId(List.of(playlistId));
        PlaylistListResponse existing = getRequest.execute();
        if (existing.getItems() == null || existing.getItems().isEmpty()) {
            throw new IllegalArgumentException("Playlist not found: " + playlistId);
        }
        Playlist playlist = existing.getItems().getFirst();

        if (title != null && !title.isBlank()) {
            playlist.getSnippet().setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            playlist.getSnippet().setDescription(description);
        }
        if (visibility != null && !visibility.isBlank()) {
            playlist.getStatus().setPrivacyStatus(visibility);
        }

        return youtube.playlists()
                .update(List.of("snippet", "status"), playlist)
                .execute();
    }

    public void deletePlaylist(String playlistId) throws IOException {
        youtube.playlists().delete(playlistId).execute();
    }

    public PlaylistItemListResponse listPlaylistItems(String playlistId, long maxResults,
                                                       String pageToken) throws IOException {
        YouTube.PlaylistItems.List request = youtube.playlistItems()
                .list(List.of("snippet", "contentDetails"))
                .setPlaylistId(playlistId)
                .setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public PlaylistItem updatePlaylistItem(String itemId, String playlistId, String videoId,
                                            Long newPosition) throws IOException {
        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setPlaylistId(playlistId);
        snippet.setPosition(newPosition);

        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);
        snippet.setResourceId(resourceId);

        PlaylistItem item = new PlaylistItem();
        item.setId(itemId);
        item.setSnippet(snippet);

        return youtube.playlistItems()
                .update(List.of("snippet"), item)
                .execute();
    }

    public void deletePlaylistItem(String itemId) throws IOException {
        youtube.playlistItems().delete(itemId).execute();
    }
}
