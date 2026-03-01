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
class ChannelServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.Channels mockChannels;
    @Mock YouTube.Channels.List mockChannelList;
    @Mock YouTube.Channels.Update mockChannelUpdate;
    @Mock YouTube.ChannelSections mockSections;
    @Mock YouTube.ChannelSections.List mockSectionList;
    @Mock YouTube.ChannelSections.Insert mockSectionInsert;
    @Mock YouTube.ChannelSections.Update mockSectionUpdate;
    @Mock YouTube.ChannelSections.Delete mockSectionDelete;

    private ChannelService service;

    @BeforeEach
    void setUp() {
        service = new ChannelService(youtube);
    }

    // ── getMyChannel ──────────────────────────────────────────────────────────

    @Test
    void getMyChannel_throwsWhenNotFound() throws IOException {
        ChannelListResponse response = new ChannelListResponse().setItems(List.of());

        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setMine(anyBoolean())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(response);

        assertThrows(IllegalArgumentException.class, () -> service.getMyChannel());
    }

    @Test
    void getMyChannel_returnsFirstChannel() throws IOException {
        Channel channel = new Channel().setId("UC1");
        ChannelListResponse response = new ChannelListResponse().setItems(List.of(channel));

        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setMine(anyBoolean())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(response);

        Channel result = service.getMyChannel();
        assertEquals("UC1", result.getId());
    }

    // ── getChannelById ────────────────────────────────────────────────────────

    @Test
    void getChannelById_throwsWhenNotFound() throws IOException {
        ChannelListResponse response = new ChannelListResponse().setItems(List.of());

        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setId(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(response);

        assertThrows(IllegalArgumentException.class,
                () -> service.getChannelById("UCmissing"));
    }

    // ── updateChannel ─────────────────────────────────────────────────────────

    @Test
    void updateChannel_appliesFields() throws IOException {
        ChannelBrandingSettings branding = new ChannelBrandingSettings()
                .setChannel(new ChannelSettings());
        Channel channel = new Channel().setId("UC1").setBrandingSettings(branding);
        ChannelListResponse getResponse = new ChannelListResponse().setItems(List.of(channel));

        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setId(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(getResponse);
        when(mockChannels.update(anyList(), captor.capture())).thenReturn(mockChannelUpdate);
        when(mockChannelUpdate.execute()).thenReturn(channel);

        service.updateChannel("UC1", "New desc", "keyword1", "US", "en");

        ChannelSettings settings = captor.getValue().getBrandingSettings().getChannel();
        assertEquals("New desc", settings.getDescription());
        assertEquals("keyword1", settings.getKeywords());
        assertEquals("US", settings.getCountry());
        assertEquals("en", settings.getDefaultLanguage());
    }

    @Test
    void updateChannel_createsNewBranding_whenNull() throws IOException {
        Channel channel = new Channel().setId("UC1").setBrandingSettings(null);
        ChannelListResponse getResponse = new ChannelListResponse().setItems(List.of(channel));

        ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
        when(youtube.channels()).thenReturn(mockChannels);
        when(mockChannels.list(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.setId(anyList())).thenReturn(mockChannelList);
        when(mockChannelList.execute()).thenReturn(getResponse);
        when(mockChannels.update(anyList(), captor.capture())).thenReturn(mockChannelUpdate);
        when(mockChannelUpdate.execute()).thenReturn(channel);

        service.updateChannel("UC1", "Desc", null, null, null);

        assertNotNull(captor.getValue().getBrandingSettings());
        assertEquals("Desc", captor.getValue().getBrandingSettings().getChannel().getDescription());
    }

    // ── insertChannelBanner ───────────────────────────────────────────────────

    @Test
    void insertChannelBanner_throwsWhenFileNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.insertChannelBanner("/nonexistent/banner.png"));
    }

    // ── setWatermark ──────────────────────────────────────────────────────────

    @Test
    void setWatermark_throwsWhenFileNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.setWatermark("UC1", "/nonexistent/watermark.png",
                        null, null, null));
    }

    // ── listChannelSections ───────────────────────────────────────────────────

    @Test
    void listChannelSections_usesMine_whenChannelIdNull() throws IOException {
        var sectionListResponse = new com.google.api.services.youtube.model.ChannelSectionListResponse()
                .setItems(List.of());

        when(youtube.channelSections()).thenReturn(mockSections);
        when(mockSections.list(anyList())).thenReturn(mockSectionList);
        when(mockSectionList.setMine(anyBoolean())).thenReturn(mockSectionList);
        when(mockSectionList.execute()).thenReturn(sectionListResponse);

        List<ChannelSection> result = service.listChannelSections(null);

        verify(mockSectionList).setMine(true);
        verify(mockSectionList, never()).setChannelId(any());
        assertTrue(result.isEmpty());
    }

    @Test
    void listChannelSections_usesChannelId_whenProvided() throws IOException {
        var sectionListResponse = new com.google.api.services.youtube.model.ChannelSectionListResponse()
                .setItems(List.of(new ChannelSection().setId("s1")));

        when(youtube.channelSections()).thenReturn(mockSections);
        when(mockSections.list(anyList())).thenReturn(mockSectionList);
        when(mockSectionList.setChannelId(any())).thenReturn(mockSectionList);
        when(mockSectionList.execute()).thenReturn(sectionListResponse);

        List<ChannelSection> result = service.listChannelSections("UC1");

        verify(mockSectionList).setChannelId("UC1");
        assertEquals(1, result.size());
    }

    // ── deleteChannelSection ──────────────────────────────────────────────────

    @Test
    void deleteChannelSection_callsDelete() throws IOException {
        when(youtube.channelSections()).thenReturn(mockSections);
        when(mockSections.delete(any())).thenReturn(mockSectionDelete);

        service.deleteChannelSection("s1");

        verify(mockSections).delete("s1");
        verify(mockSectionDelete).execute();
    }
}
