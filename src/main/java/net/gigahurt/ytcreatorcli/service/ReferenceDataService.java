package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.I18nLanguage;
import com.google.api.services.youtube.model.I18nRegion;
import com.google.api.services.youtube.model.MemberListResponse;
import com.google.api.services.youtube.model.SuperChatEventListResponse;
import com.google.api.services.youtube.model.VideoCategory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ReferenceDataService {

    private final YouTube youtube;

    public ReferenceDataService(YouTube youtube) {
        this.youtube = youtube;
    }

    public List<VideoCategory> listVideoCategories(String regionCode, String language) throws IOException {
        var response = youtube.videoCategories()
                .list(List.of("snippet"))
                .setRegionCode(regionCode != null ? regionCode : "US")
                .setHl(language != null ? language : "en")
                .execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public List<I18nLanguage> listLanguages(String language) throws IOException {
        var response = youtube.i18nLanguages()
                .list(List.of("snippet"))
                .setHl(language != null ? language : "en")
                .execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public List<I18nRegion> listRegions(String language) throws IOException {
        var response = youtube.i18nRegions()
                .list(List.of("snippet"))
                .setHl(language != null ? language : "en")
                .execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public SuperChatEventListResponse listSuperChatEvents(long maxResults, String pageToken) throws IOException {
        YouTube.SuperChatEvents.List request = youtube.superChatEvents()
                .list(List.of("snippet"))
                .setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public MemberListResponse listMembers(String mode, long maxResults, String pageToken) throws IOException {
        YouTube.Members.List request = youtube.members()
                .list(List.of("snippet"))
                .setMode(mode != null ? mode : "listMembers")
                .setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }
}
