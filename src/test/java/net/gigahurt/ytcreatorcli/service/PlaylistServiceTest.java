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
class PlaylistServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Playlists mockPlaylists;
    @Mock YouTube.Playlists.Insert mockPlaylistInsert;
    @Mock YouTube.Playlists.List mockPlaylistList;
    @Mock YouTube.Playlists.Update mockPlaylistUpdate;
    @Mock YouTube.Playlists.Delete mockPlaylistDelete;
    @Mock YouTube.PlaylistItems mockPlaylistItems;
    @Mock YouTube.PlaylistItems.Insert mockItemInsert;
    @Mock YouTube.PlaylistItems.List mockItemList;
    @Mock YouTube.PlaylistItems.Update mockItemUpdate;
    @Mock YouTube.PlaylistItems.Delete mockItemDelete;

    private PlaylistService service;

    @BeforeEach
    void setUp() {
        service = new PlaylistService(youtube);
    }

    // ── createPlaylist ────────────────────────────────────────────────────────

    @Test
    void createPlaylist_setsDefaultPrivateVisibility_whenNull() throws IOException {
        ArgumentCaptor<Playlist> captor = ArgumentCaptor.forClass(Playlist.class);
        Playlist created = new Playlist().setId("pl1");

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.insert(anyList(), captor.capture())).thenReturn(mockPlaylistInsert);
        when(mockPlaylistInsert.execute()).thenReturn(created);

        service.createPlaylist("My List", "Desc", null);

        Playlist captured = captor.getValue();
        assertEquals("private", captured.getStatus().getPrivacyStatus());
        assertEquals("My List", captured.getSnippet().getTitle());
        assertEquals("Desc", captured.getSnippet().getDescription());
    }

    @Test
    void createPlaylist_omitsDescription_whenBlank() throws IOException {
        ArgumentCaptor<Playlist> captor = ArgumentCaptor.forClass(Playlist.class);
        Playlist created = new Playlist().setId("pl1");

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.insert(anyList(), captor.capture())).thenReturn(mockPlaylistInsert);
        when(mockPlaylistInsert.execute()).thenReturn(created);

        service.createPlaylist("List", "  ", "public");

        assertNull(captor.getValue().getSnippet().getDescription());
        assertEquals("public", captor.getValue().getStatus().getPrivacyStatus());
    }

    // ── listPlaylists ─────────────────────────────────────────────────────────

    @Test
    void listPlaylists_usesChannelId_whenProvided() throws IOException {
        PlaylistListResponse response = new PlaylistListResponse().setItems(List.of());

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.list(anyList())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setChannelId(any())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setMaxResults(anyLong())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.execute()).thenReturn(response);

        service.listPlaylists("UCtest", 25, null);

        verify(mockPlaylistList).setChannelId("UCtest");
        verify(mockPlaylistList, never()).setMine(anyBoolean());
    }

    @Test
    void listPlaylists_usesMine_whenChannelIdBlank() throws IOException {
        PlaylistListResponse response = new PlaylistListResponse().setItems(List.of());

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.list(anyList())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setMine(anyBoolean())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setMaxResults(anyLong())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.execute()).thenReturn(response);

        service.listPlaylists(null, 25, null);

        verify(mockPlaylistList).setMine(true);
        verify(mockPlaylistList, never()).setChannelId(any());
    }

    @Test
    void listPlaylists_setsPageToken_whenProvided() throws IOException {
        PlaylistListResponse response = new PlaylistListResponse().setItems(List.of());

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.list(anyList())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setChannelId(any())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setMaxResults(anyLong())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setPageToken(any())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.execute()).thenReturn(response);

        service.listPlaylists("UCtest", 25, "tokenABC");

        verify(mockPlaylistList).setPageToken("tokenABC");
    }

    // ── updatePlaylist ────────────────────────────────────────────────────────

    @Test
    void updatePlaylist_throwsWhenNotFound() throws IOException {
        PlaylistListResponse getResponse = new PlaylistListResponse().setItems(List.of());

        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.list(anyList())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.setId(anyList())).thenReturn(mockPlaylistList);
        when(mockPlaylistList.execute()).thenReturn(getResponse);

        assertThrows(IllegalArgumentException.class,
                () -> service.updatePlaylist("pl_missing", "New Title", null, null));
    }

    // ── deletePlaylist ────────────────────────────────────────────────────────

    @Test
    void deletePlaylist_callsApiDelete() throws IOException {
        when(youtube.playlists()).thenReturn(mockPlaylists);
        when(mockPlaylists.delete(any())).thenReturn(mockPlaylistDelete);

        service.deletePlaylist("pl1");

        verify(mockPlaylists).delete("pl1");
        verify(mockPlaylistDelete).execute();
    }

    // ── addToPlaylist ─────────────────────────────────────────────────────────

    @Test
    void addToPlaylist_setsPosition_whenProvided() throws IOException {
        ArgumentCaptor<PlaylistItem> captor = ArgumentCaptor.forClass(PlaylistItem.class);
        PlaylistItem item = new PlaylistItem();

        when(youtube.playlistItems()).thenReturn(mockPlaylistItems);
        when(mockPlaylistItems.insert(anyList(), captor.capture())).thenReturn(mockItemInsert);
        when(mockItemInsert.execute()).thenReturn(item);

        service.addToPlaylist("pl1", "v1", 3L);

        PlaylistItemSnippet snippet = captor.getValue().getSnippet();
        assertEquals(3L, snippet.getPosition());
        assertEquals("pl1", snippet.getPlaylistId());
        assertEquals("v1", snippet.getResourceId().getVideoId());
        assertEquals("youtube#video", snippet.getResourceId().getKind());
    }

    @Test
    void addToPlaylist_omitsPosition_whenNull() throws IOException {
        ArgumentCaptor<PlaylistItem> captor = ArgumentCaptor.forClass(PlaylistItem.class);
        PlaylistItem item = new PlaylistItem();

        when(youtube.playlistItems()).thenReturn(mockPlaylistItems);
        when(mockPlaylistItems.insert(anyList(), captor.capture())).thenReturn(mockItemInsert);
        when(mockItemInsert.execute()).thenReturn(item);

        service.addToPlaylist("pl1", "v2", null);

        assertNull(captor.getValue().getSnippet().getPosition());
    }
}
