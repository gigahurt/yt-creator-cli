package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import net.gigahurt.ytcreatorcli.model.VideoDetail;
import net.gigahurt.ytcreatorcli.model.VideoSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YouTubeServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Videos mockVideos;
    @Mock YouTube.Videos.List mockVideoList;
    @Mock YouTube.Videos.Update mockVideoUpdate;
    @Mock YouTube.Search mockSearch;
    @Mock YouTube.Search.List mockSearchList;
    @Mock YouTube.Channels mockChannels;
    @Mock YouTube.Channels.List mockChannelList;
    @Mock YouTube.PlaylistItems mockPlaylistItems;
    @Mock YouTube.PlaylistItems.List mockPlaylistItemList;

    private YouTubeService service;

    @BeforeEach
    void setUp() {
        service = new YouTubeService(youtube);
    }

    // ── searchVideos ──────────────────────────────────────────────────────────

    @Test
    void searchVideos_withQuery_setsQ() throws IOException {
        ResourceId id = new ResourceId().setVideoId("vid1");
        SearchResultSnippet snippet = new SearchResultSnippet()
                .setTitle("Title")
                .setDescription("Desc")
                .setPublishedAt(new DateTime(0L));
        SearchResult item = new SearchResult().setId(id).setSnippet(snippet);
        SearchListResponse response = new SearchListResponse().setItems(List.of(item));

        when(youtube.search()).thenReturn(mockSearch);
        when(mockSearch.list(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(any())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(any())).thenReturn(mockSearchList);
        when(mockSearchList.setQ(any())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenReturn(response);

        List<VideoSearchResult> results = service.searchVideos("UCtest", "hello", 10);

        assertEquals(1, results.size());
        assertEquals("vid1", results.get(0).videoId());
        assertEquals("Title", results.get(0).title());
        verify(mockSearchList).setQ("hello");
    }

    @Test
    void searchVideos_withBlankQuery_doesNotSetQ() throws IOException {
        ResourceId id = new ResourceId().setVideoId("v1");
        SearchResultSnippet snippet = new SearchResultSnippet()
                .setTitle("T").setDescription("D").setPublishedAt(new DateTime(0L));
        SearchListResponse response = new SearchListResponse()
                .setItems(List.of(new SearchResult().setId(id).setSnippet(snippet)));

        when(youtube.search()).thenReturn(mockSearch);
        when(mockSearch.list(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setChannelId(any())).thenReturn(mockSearchList);
        when(mockSearchList.setType(anyList())).thenReturn(mockSearchList);
        when(mockSearchList.setMaxResults(anyLong())).thenReturn(mockSearchList);
        when(mockSearchList.setOrder(any())).thenReturn(mockSearchList);
        when(mockSearchList.execute()).thenReturn(response);

        service.searchVideos("UCtest", "  ", 5);

        verify(mockSearchList, never()).setQ(any());
    }

    // ── listChannelVideos ─────────────────────────────────────────────────────

    @Test
    void listChannelVideos_throwsWhenChannelNotFound() throws IOException {
        ChannelListResponse channelResponse = new ChannelListResponse().setItems(List.of());

        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setId(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(channelResponse);

        assertThrows(IllegalArgumentException.class,
                () -> service.listChannelVideos("UCmissing", null, 10));
    }

    @Test
    void listChannelVideos_filtersResultsByQuery() throws IOException {
        // Channel lookup
        ChannelContentDetails.RelatedPlaylists playlists =
                new ChannelContentDetails.RelatedPlaylists().setUploads("PLuploads");
        ChannelContentDetails contentDetails = new ChannelContentDetails().setRelatedPlaylists(playlists);
        Channel channel = new Channel().setContentDetails(contentDetails);
        ChannelListResponse channelResponse = new ChannelListResponse().setItems(List.of(channel));

        // Playlist items – one matching, one not
        PlaylistItemSnippet matchSnippet = new PlaylistItemSnippet()
                .setTitle("Kotlin Tutorial").setDescription("about kotlin")
                .setResourceId(new ResourceId().setVideoId("v1"))
                .setPublishedAt(new DateTime(0L));
        PlaylistItemSnippet noMatchSnippet = new PlaylistItemSnippet()
                .setTitle("Java Video").setDescription("about java")
                .setResourceId(new ResourceId().setVideoId("v2"))
                .setPublishedAt(new DateTime(0L));
        PlaylistItemListResponse playlistResponse = new PlaylistItemListResponse()
                .setItems(List.of(
                        new PlaylistItem().setSnippet(matchSnippet),
                        new PlaylistItem().setSnippet(noMatchSnippet)
                ));

        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setId(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(channelResponse);

        when(youtube.playlistItems()).thenReturn(mockPlaylistItems);
        when(mockPlaylistItems.list(anyList())).thenReturn(mockPlaylistItemList);
        when(mockPlaylistItemList.setPlaylistId(any())).thenReturn(mockPlaylistItemList);
        when(mockPlaylistItemList.setMaxResults(anyLong())).thenReturn(mockPlaylistItemList);
        when(mockPlaylistItemList.setPageToken(any())).thenReturn(mockPlaylistItemList);
        when(mockPlaylistItemList.execute()).thenReturn(playlistResponse);

        List<VideoSearchResult> results = service.listChannelVideos("UCtest", "kotlin", 10);

        assertEquals(1, results.size());
        assertEquals("v1", results.get(0).videoId());
    }

    // ── getVideo ──────────────────────────────────────────────────────────────

    @Test
    void getVideo_throwsWhenNotFound() throws IOException {
        VideoListResponse response = new VideoListResponse().setItems(List.of());

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.list(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.setId(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.execute()).thenReturn(response);

        assertThrows(IllegalArgumentException.class, () -> service.getVideo("missing"));
    }

    @Test
    void getVideo_returnsFirstItem() throws IOException {
        Video video = new Video().setId("v1")
                .setSnippet(new VideoSnippet().setTitle("T"));
        VideoListResponse response = new VideoListResponse().setItems(List.of(video));

        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.list(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.setId(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.execute()).thenReturn(response);

        Video result = service.getVideo("v1");
        assertEquals("v1", result.getId());
    }

    // ── toVideoDetail ─────────────────────────────────────────────────────────

    @Test
    void toVideoDetail_mapsAllFields() {
        VideoSnippet snippet = new VideoSnippet()
                .setTitle("Title").setDescription("Desc")
                .setTags(List.of("a", "b")).setCategoryId("22")
                .setDefaultLanguage("en").setDefaultAudioLanguage("en")
                .setPublishedAt(new DateTime(1_000_000L));
        VideoStatus status = new VideoStatus()
                .setPrivacyStatus("public")
                .setSelfDeclaredMadeForKids(false)
                .setPublishAt(new DateTime(2_000_000L));
        Video video = new Video().setId("v1").setSnippet(snippet).setStatus(status);

        VideoDetail detail = service.toVideoDetail(video);

        assertEquals("v1", detail.videoId());
        assertEquals("Title", detail.title());
        assertEquals("Desc", detail.description());
        assertEquals(List.of("a", "b"), detail.tags());
        assertEquals("22", detail.categoryId());
        assertEquals("en", detail.defaultLanguage());
        assertEquals("public", detail.visibility());
        assertFalse(detail.madeForKids());
        assertNotNull(detail.publishAt());
        assertNotNull(detail.publishedAt());
    }

    @Test
    void toVideoDetail_handlesNullStatus() {
        VideoSnippet snippet = new VideoSnippet().setTitle("T").setDescription("D");
        Video video = new Video().setId("v1").setSnippet(snippet).setStatus(null);

        VideoDetail detail = service.toVideoDetail(video);

        assertNull(detail.visibility());
        assertNull(detail.madeForKids());
        assertNull(detail.publishAt());
    }

    // ── updateVideo ───────────────────────────────────────────────────────────

    @Test
    void updateVideo_setsPrivacyToPrivate_whenPublishAtGiven() throws IOException {
        VideoSnippet snippet = new VideoSnippet().setTitle("Old");
        VideoStatus status = new VideoStatus().setPrivacyStatus("public");
        Video video = new Video().setId("v1").setSnippet(snippet).setStatus(status);
        VideoListResponse getResponse = new VideoListResponse().setItems(List.of(video));

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.list(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.setId(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.execute()).thenReturn(getResponse);
        when(mockVideos.update(anyList(), videoCaptor.capture())).thenReturn(mockVideoUpdate);
        when(mockVideoUpdate.execute()).thenReturn(video);

        service.updateVideo("v1", null, null, null, null, null, null, "public",
                null, "2025-12-31T00:00:00Z");

        Video captured = videoCaptor.getValue();
        assertEquals("private", captured.getStatus().getPrivacyStatus());
        assertNotNull(captured.getStatus().getPublishAt());
    }

    @Test
    void updateVideo_usesVisibility_whenNoPublishAt() throws IOException {
        VideoSnippet snippet = new VideoSnippet().setTitle("Old");
        VideoStatus status = new VideoStatus().setPrivacyStatus("private");
        Video video = new Video().setId("v1").setSnippet(snippet).setStatus(status);
        VideoListResponse getResponse = new VideoListResponse().setItems(List.of(video));

        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        when(youtube.videos()).thenReturn(mockVideos);
        when(mockVideos.list(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.setId(anyList())).thenReturn(mockVideoList);
        when(mockVideoList.execute()).thenReturn(getResponse);
        when(mockVideos.update(anyList(), videoCaptor.capture())).thenReturn(mockVideoUpdate);
        when(mockVideoUpdate.execute()).thenReturn(video);

        service.updateVideo("v1", "New Title", null, null, null, null, null, "unlisted", null, null);

        Video captured = videoCaptor.getValue();
        assertEquals("New Title", captured.getSnippet().getTitle());
        assertEquals("unlisted", captured.getStatus().getPrivacyStatus());
    }
}
