package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.I18nLanguage;
import com.google.api.services.youtube.model.I18nRegion;
import com.google.api.services.youtube.model.Member;
import com.google.api.services.youtube.model.MemberListResponse;
import com.google.api.services.youtube.model.SuperChatEvent;
import com.google.api.services.youtube.model.SuperChatEventListResponse;
import com.google.api.services.youtube.model.VideoCategory;

import net.gigahurt.ytcreatorcli.service.ReferenceDataService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReferenceCommands {

    private final ReferenceDataService referenceDataService;

    public ReferenceCommands(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @Command(name = "list-categories", description = "List YouTube video categories by region")
    public String listCategories(
            @Option(longName = "region", description = "ISO 3166-1 alpha-2 region code (e.g. US, GB)", defaultValue = "US") String regionCode,
            @Option(longName = "language", description = "Language for category names (e.g. en)", defaultValue = "en") String language) {
        try {
            List<VideoCategory> categories = referenceDataService.listVideoCategories(regionCode, language);
            if (categories.isEmpty()) {
                return "No categories found for region: " + regionCode;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Video categories (%s/%s):\n", regionCode, language));
            for (VideoCategory c : categories) {
                if (c.getSnippet() != null && Boolean.TRUE.equals(c.getSnippet().getAssignable())) {
                    sb.append(String.format("  %-4s %s\n", c.getId(), c.getSnippet().getTitle()));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-languages", description = "List supported i18n languages")
    public String listLanguages(
            @Option(longName = "language", description = "Language for name display (e.g. en)", defaultValue = "en") String language) {
        try {
            List<I18nLanguage> languages = referenceDataService.listLanguages(language);
            if (languages.isEmpty()) {
                return "No languages found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Supported languages (%d):\n", languages.size()));
            for (I18nLanguage l : languages) {
                sb.append(String.format("  %-8s %s\n",
                        l.getId(),
                        l.getSnippet() != null ? l.getSnippet().getName() : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-regions", description = "List supported i18n regions")
    public String listRegions(
            @Option(longName = "language", description = "Language for name display (e.g. en)", defaultValue = "en") String language) {
        try {
            List<I18nRegion> regions = referenceDataService.listRegions(language);
            if (regions.isEmpty()) {
                return "No regions found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Supported regions (%d):\n", regions.size()));
            for (I18nRegion r : regions) {
                sb.append(String.format("  %-4s %s\n",
                        r.getId(),
                        r.getSnippet() != null ? r.getSnippet().getName() : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-superchats", description = "List Super Chat events on your channel")
    public String listSuperChats(
            @Option(longName = "max-results", description = "Max results", defaultValue = "25") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            SuperChatEventListResponse response = referenceDataService.listSuperChatEvents(
                    maxResults, (pageToken == null || pageToken.isEmpty()) ? null : pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No Super Chat events found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Super Chat events (%d):\n", response.getItems().size()));
            for (SuperChatEvent e : response.getItems()) {
                var s = e.getSnippet();
                if (s != null) {
                    sb.append(String.format("  By: %-20s  Amount: %-10s  Message: %s  Time: %s\n",
                            s.getSupporterDetails() != null ? s.getSupporterDetails().getDisplayName() : "",
                            s.getDisplayString() != null ? s.getDisplayString() : "",
                            s.getCommentText() != null ? truncate(s.getCommentText(), 80) : "",
                            s.getCreatedAt() != null ? s.getCreatedAt().toString() : ""));
                }
            }
            if (response.getNextPageToken() != null) {
                sb.append("---\nNext page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-members", description = "List channel members (sponsors)")
    public String listMembers(
            @Option(longName = "max-results", description = "Max results", defaultValue = "25") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            MemberListResponse response = referenceDataService.listMembers(
                    "listMembers", maxResults, (pageToken == null || pageToken.isEmpty()) ? null : pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No channel members found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Channel members (%d):\n", response.getItems().size()));
            for (Member m : response.getItems()) {
                var s = m.getSnippet();
                if (s != null) {
                    var details = s.getMembershipsDetails();
                    var duration = details != null ? details.getMembershipsDuration() : null;
                    sb.append(String.format("  Channel: %-30s  Level: %s  Since: %s\n",
                            s.getMemberDetails() != null ? s.getMemberDetails().getDisplayName() : "",
                            details != null ? details.getHighestAccessibleLevelDisplayName() : "",
                            duration != null ? duration.getMemberSince() : ""));
                }
            }
            if (response.getNextPageToken() != null) {
                sb.append("---\nNext page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
