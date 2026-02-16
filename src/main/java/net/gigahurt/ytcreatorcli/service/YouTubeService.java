package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import net.gigahurt.ytcreatorcli.model.VideoDetail;
import net.gigahurt.ytcreatorcli.model.VideoSearchResult;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeService {

    private final YouTube youtube;

    public YouTubeService(YouTube youtube) {
        this.youtube = youtube;
    }

    public List<VideoSearchResult> searchVideos(String channelId, String query,
                                                 long maxResults) throws IOException {
        YouTube.Search.List request = youtube.search()
                .list(List.of("id", "snippet"));
        request.setChannelId(channelId);
        request.setType(List.of("video"));
        request.setMaxResults(maxResults);
        request.setOrder("date");
        if (query != null && !query.isBlank()) {
            request.setQ(query);
        }

        SearchListResponse response = request.execute();
        return response.getItems().stream()
                .map(item -> new VideoSearchResult(
                        item.getId().getVideoId(),
                        item.getSnippet().getTitle(),
                        item.getSnippet().getDescription(),
                        item.getSnippet().getPublishedAt().toString()))
                .toList();
    }

    public List<VideoSearchResult> listChannelVideos(String channelId, String query,
                                                       long maxResults) throws IOException {
        // Get the uploads playlist ID for this channel
        ChannelListResponse channelResponse = youtube.channels()
                .list(List.of("contentDetails"))
                .setId(List.of(channelId))
                .execute();

        if (channelResponse.getItems().isEmpty()) {
            throw new IllegalArgumentException("Channel not found: " + channelId);
        }

        String uploadsPlaylistId = channelResponse.getItems().getFirst()
                .getContentDetails().getRelatedPlaylists().getUploads();

        // Page through the uploads playlist
        List<VideoSearchResult> results = new ArrayList<>();
        String pageToken = null;
        String lowerQuery = (query != null && !query.isBlank()) ? query.toLowerCase() : null;

        do {
            PlaylistItemListResponse playlistResponse = youtube.playlistItems()
                    .list(List.of("snippet"))
                    .setPlaylistId(uploadsPlaylistId)
                    .setMaxResults(50L)
                    .setPageToken(pageToken)
                    .execute();

            for (var item : playlistResponse.getItems()) {
                var snippet = item.getSnippet();
                String title = snippet.getTitle();
                String description = snippet.getDescription();

                if (lowerQuery != null
                        && !title.toLowerCase().contains(lowerQuery)
                        && !description.toLowerCase().contains(lowerQuery)) {
                    continue;
                }

                results.add(new VideoSearchResult(
                        snippet.getResourceId().getVideoId(),
                        title,
                        description,
                        snippet.getPublishedAt() != null ? snippet.getPublishedAt().toString() : ""));
            }

            pageToken = playlistResponse.getNextPageToken();
        } while (pageToken != null && results.size() < maxResults);

        return results.stream().limit(maxResults).toList();
    }

    public Video getVideo(String videoId) throws IOException {
        VideoListResponse response = youtube.videos()
                .list(List.of("snippet", "status"))
                .setId(List.of(videoId))
                .execute();

        if (response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Video not found: " + videoId);
        }
        return response.getItems().getFirst();
    }

    public VideoDetail toVideoDetail(Video video) {
        VideoSnippet s = video.getSnippet();
        VideoStatus st = video.getStatus();
        return new VideoDetail(
                video.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getTags(),
                s.getCategoryId(),
                s.getDefaultLanguage(),
                s.getDefaultAudioLanguage(),
                st != null ? st.getPrivacyStatus() : null,
                st != null ? st.getSelfDeclaredMadeForKids() : null,
                st != null && st.getPublishAt() != null ? st.getPublishAt().toString() : null,
                s.getPublishedAt() != null ? s.getPublishedAt().toString() : null
        );
    }

    public VideoDetail updateVideo(String videoId, String title, String description,
                                    List<String> tags, String categoryId,
                                    String defaultLanguage, String defaultAudioLanguage,
                                    String visibility,
                                    Boolean madeForKids, String publishAt) throws IOException {
        Video video = getVideo(videoId);
        VideoSnippet snippet = video.getSnippet();

        if (title != null && !title.isBlank()) {
            snippet.setTitle(title);
        }
        if (description != null) {
            snippet.setDescription(description);
        }
        if (tags != null && !tags.isEmpty()) {
            snippet.setTags(tags);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            snippet.setCategoryId(categoryId);
        }
        if (defaultLanguage != null && !defaultLanguage.isBlank()) {
            snippet.setDefaultLanguage(defaultLanguage);
        }
        if (defaultAudioLanguage != null && !defaultAudioLanguage.isBlank()) {
            snippet.setDefaultAudioLanguage(defaultAudioLanguage);
        }

        VideoStatus status = video.getStatus();
        if (status == null) {
            status = new VideoStatus();
            video.setStatus(status);
        }

        if (publishAt != null && !publishAt.isBlank()) {
            status.setPublishAt(DateTime.parseRfc3339(publishAt));
            status.setPrivacyStatus("private");
        } else if (visibility != null && !visibility.isBlank()) {
            status.setPrivacyStatus(visibility);
        }

        if (madeForKids != null) {
            status.setSelfDeclaredMadeForKids(madeForKids);
        }

        List<String> updateParts = List.of("snippet", "status");
        Video updated = youtube.videos()
                .update(updateParts, video)
                .execute();

        return toVideoDetail(updated);
    }
}
