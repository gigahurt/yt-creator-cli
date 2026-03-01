package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceTest {

    @Mock YouTube youtube;
    @Mock YouTube.VideoCategories mockCategories;
    @Mock YouTube.VideoCategories.List mockCategoryList;
    @Mock YouTube.I18nLanguages mockLanguages;
    @Mock YouTube.I18nLanguages.List mockLanguageList;
    @Mock YouTube.I18nRegions mockRegions;
    @Mock YouTube.I18nRegions.List mockRegionList;
    @Mock YouTube.SuperChatEvents mockSuperChat;
    @Mock YouTube.SuperChatEvents.List mockSuperChatList;
    @Mock YouTube.Members mockMembers;
    @Mock YouTube.Members.List mockMemberList;

    private ReferenceDataService service;

    @BeforeEach
    void setUp() {
        service = new ReferenceDataService(youtube);
    }

    // ── listVideoCategories ───────────────────────────────────────────────────

    @Test
    void listVideoCategories_defaultsToUS_en_whenNull() throws IOException {
        VideoCategoryListResponse response = new VideoCategoryListResponse()
                .setItems(List.of(new VideoCategory().setId("22")));

        when(youtube.videoCategories()).thenReturn(mockCategories);
        when(mockCategories.list(anyList())).thenReturn(mockCategoryList);
        when(mockCategoryList.setRegionCode(any())).thenReturn(mockCategoryList);
        when(mockCategoryList.setHl(any())).thenReturn(mockCategoryList);
        when(mockCategoryList.execute()).thenReturn(response);

        List<VideoCategory> result = service.listVideoCategories(null, null);

        verify(mockCategoryList).setRegionCode("US");
        verify(mockCategoryList).setHl("en");
        assertEquals(1, result.size());
    }

    @Test
    void listVideoCategories_usesProvidedParams() throws IOException {
        VideoCategoryListResponse response = new VideoCategoryListResponse().setItems(List.of());

        when(youtube.videoCategories()).thenReturn(mockCategories);
        when(mockCategories.list(anyList())).thenReturn(mockCategoryList);
        when(mockCategoryList.setRegionCode(any())).thenReturn(mockCategoryList);
        when(mockCategoryList.setHl(any())).thenReturn(mockCategoryList);
        when(mockCategoryList.execute()).thenReturn(response);

        service.listVideoCategories("GB", "fr");

        verify(mockCategoryList).setRegionCode("GB");
        verify(mockCategoryList).setHl("fr");
    }

    // ── listLanguages ─────────────────────────────────────────────────────────

    @Test
    void listLanguages_returnsEmptyList_whenItemsNull() throws IOException {
        I18nLanguageListResponse response = new I18nLanguageListResponse().setItems(null);

        when(youtube.i18nLanguages()).thenReturn(mockLanguages);
        when(mockLanguages.list(anyList())).thenReturn(mockLanguageList);
        when(mockLanguageList.setHl(any())).thenReturn(mockLanguageList);
        when(mockLanguageList.execute()).thenReturn(response);

        assertTrue(service.listLanguages(null).isEmpty());
    }

    // ── listRegions ───────────────────────────────────────────────────────────

    @Test
    void listRegions_defaultsToEn_whenNull() throws IOException {
        I18nRegionListResponse response = new I18nRegionListResponse().setItems(List.of());

        when(youtube.i18nRegions()).thenReturn(mockRegions);
        when(mockRegions.list(anyList())).thenReturn(mockRegionList);
        when(mockRegionList.setHl(any())).thenReturn(mockRegionList);
        when(mockRegionList.execute()).thenReturn(response);

        service.listRegions(null);

        verify(mockRegionList).setHl("en");
    }

    // ── listSuperChatEvents ───────────────────────────────────────────────────

    @Test
    void listSuperChatEvents_setsPageToken_whenProvided() throws IOException {
        SuperChatEventListResponse response = new SuperChatEventListResponse();

        when(youtube.superChatEvents()).thenReturn(mockSuperChat);
        when(mockSuperChat.list(anyList())).thenReturn(mockSuperChatList);
        when(mockSuperChatList.setMaxResults(anyLong())).thenReturn(mockSuperChatList);
        when(mockSuperChatList.setPageToken(any())).thenReturn(mockSuperChatList);
        when(mockSuperChatList.execute()).thenReturn(response);

        service.listSuperChatEvents(10, "tok1");

        verify(mockSuperChatList).setPageToken("tok1");
    }

    // ── listMembers ───────────────────────────────────────────────────────────

    @Test
    void listMembers_defaultsToListMembers_whenModeNull() throws IOException {
        MemberListResponse response = new MemberListResponse();

        when(youtube.members()).thenReturn(mockMembers);
        when(mockMembers.list(anyList())).thenReturn(mockMemberList);
        when(mockMemberList.setMode(any())).thenReturn(mockMemberList);
        when(mockMemberList.setMaxResults(anyLong())).thenReturn(mockMemberList);
        when(mockMemberList.execute()).thenReturn(response);

        service.listMembers(null, 20, null);

        verify(mockMemberList).setMode("listMembers");
    }
}
