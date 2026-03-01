package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
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
class LiveStreamingServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.LiveBroadcasts mockBroadcasts;
    @Mock YouTube.LiveBroadcasts.Insert mockBroadcastInsert;
    @Mock YouTube.LiveBroadcasts.List mockBroadcastList;
    @Mock YouTube.LiveBroadcasts.Update mockBroadcastUpdate;
    @Mock YouTube.LiveBroadcasts.Delete mockBroadcastDelete;
    @Mock YouTube.LiveBroadcasts.Bind mockBroadcastBind;
    @Mock YouTube.LiveBroadcasts.Transition mockBroadcastTransition;
    @Mock YouTube.LiveStreams mockStreams;
    @Mock YouTube.LiveStreams.Insert mockStreamInsert;
    @Mock YouTube.LiveStreams.List mockStreamList;
    @Mock YouTube.LiveStreams.Update mockStreamUpdate;
    @Mock YouTube.LiveStreams.Delete mockStreamDelete;

    private LiveStreamingService service;

    @BeforeEach
    void setUp() {
        service = new LiveStreamingService(youtube);
    }

    // ── createBroadcast ───────────────────────────────────────────────────────

    @Test
    void createBroadcast_defaultsToPrivate_whenNullPrivacyStatus() throws IOException {
        ArgumentCaptor<LiveBroadcast> captor = ArgumentCaptor.forClass(LiveBroadcast.class);
        LiveBroadcast created = new LiveBroadcast();

        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.insert(anyList(), captor.capture())).thenReturn(mockBroadcastInsert);
        when(mockBroadcastInsert.execute()).thenReturn(created);

        service.createBroadcast("Title", null, "2025-12-31T20:00:00Z", null, true, false);

        assertEquals("private", captor.getValue().getStatus().getPrivacyStatus());
        assertTrue(captor.getValue().getContentDetails().getEnableAutoStart());
        assertFalse(captor.getValue().getContentDetails().getEnableAutoStop());
    }

    @Test
    void createBroadcast_setsAllProvidedFields() throws IOException {
        ArgumentCaptor<LiveBroadcast> captor = ArgumentCaptor.forClass(LiveBroadcast.class);
        LiveBroadcast created = new LiveBroadcast();

        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.insert(anyList(), captor.capture())).thenReturn(mockBroadcastInsert);
        when(mockBroadcastInsert.execute()).thenReturn(created);

        service.createBroadcast("Stream Title", "A description",
                "2025-12-31T20:00:00Z", "public", false, true);

        LiveBroadcast captured = captor.getValue();
        assertEquals("Stream Title", captured.getSnippet().getTitle());
        assertEquals("A description", captured.getSnippet().getDescription());
        assertEquals("public", captured.getStatus().getPrivacyStatus());
    }

    // ── listBroadcasts ────────────────────────────────────────────────────────

    @Test
    void listBroadcasts_defaultsToUpcoming_whenNull() throws IOException {
        LiveBroadcastListResponse response = new LiveBroadcastListResponse().setItems(List.of());

        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.list(anyList())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.setBroadcastStatus(any())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.setMaxResults(anyLong())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.execute()).thenReturn(response);

        service.listBroadcasts(null, 10);

        verify(mockBroadcastList).setBroadcastStatus("upcoming");
    }

    @Test
    void listBroadcasts_returnsEmptyList_whenItemsNull() throws IOException {
        LiveBroadcastListResponse response = new LiveBroadcastListResponse().setItems(null);

        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.list(anyList())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.setBroadcastStatus(any())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.setMaxResults(anyLong())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.execute()).thenReturn(response);

        assertTrue(service.listBroadcasts("active", 5).isEmpty());
    }

    // ── updateBroadcast ───────────────────────────────────────────────────────

    @Test
    void updateBroadcast_throwsWhenNotFound() throws IOException {
        LiveBroadcastListResponse response = new LiveBroadcastListResponse().setItems(List.of());

        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.list(anyList())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.setId(anyList())).thenReturn(mockBroadcastList);
        when(mockBroadcastList.execute()).thenReturn(response);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateBroadcast("b_missing", "T", null, null, null));
    }

    // ── deleteBroadcast ───────────────────────────────────────────────────────

    @Test
    void deleteBroadcast_callsApiDelete() throws IOException {
        when(youtube.liveBroadcasts()).thenReturn(mockBroadcasts);
        when(mockBroadcasts.delete(any())).thenReturn(mockBroadcastDelete);

        service.deleteBroadcast("b1");

        verify(mockBroadcasts).delete("b1");
        verify(mockBroadcastDelete).execute();
    }

    // ── createStream ──────────────────────────────────────────────────────────

    @Test
    void createStream_appliesDefaults_whenNullParams() throws IOException {
        ArgumentCaptor<LiveStream> captor = ArgumentCaptor.forClass(LiveStream.class);
        LiveStream created = new LiveStream();

        when(youtube.liveStreams()).thenReturn(mockStreams);
        when(mockStreams.insert(anyList(), captor.capture())).thenReturn(mockStreamInsert);
        when(mockStreamInsert.execute()).thenReturn(created);

        service.createStream("My Stream", null, null, null);

        CdnSettings cdn = captor.getValue().getCdn();
        assertEquals("variable", cdn.getFrameRate());
        assertEquals("variable", cdn.getResolution());
        assertEquals("rtmp", cdn.getIngestionType());
    }

    // ── updateStream ──────────────────────────────────────────────────────────

    @Test
    void updateStream_throwsWhenNotFound() throws IOException {
        LiveStreamListResponse response = new LiveStreamListResponse().setItems(List.of());

        when(youtube.liveStreams()).thenReturn(mockStreams);
        when(mockStreams.list(anyList())).thenReturn(mockStreamList);
        when(mockStreamList.setId(anyList())).thenReturn(mockStreamList);
        when(mockStreamList.execute()).thenReturn(response);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStream("s_missing", "New Title"));
    }

    // ── deleteStream ──────────────────────────────────────────────────────────

    @Test
    void deleteStream_callsApiDelete() throws IOException {
        when(youtube.liveStreams()).thenReturn(mockStreams);
        when(mockStreams.delete(any())).thenReturn(mockStreamDelete);

        service.deleteStream("s1");

        verify(mockStreams).delete("s1");
        verify(mockStreamDelete).execute();
    }
}
