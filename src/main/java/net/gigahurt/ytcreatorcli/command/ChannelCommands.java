package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelSection;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;

import net.gigahurt.ytcreatorcli.config.AppProperties;
import net.gigahurt.ytcreatorcli.service.ChannelService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelCommands {

    private final ChannelService channelService;
    private final AppProperties props;

    public ChannelCommands(ChannelService channelService, AppProperties props) {
        this.channelService = channelService;
        this.props = props;
    }

    private String resolveChannelId(String channelId) {
        if (channelId != null && !channelId.isEmpty()) {
            return channelId;
        }
        if (props.channelId() != null && !props.channelId().isEmpty()) {
            return props.channelId();
        }
        return null; // service will use mine=true
    }

    @Command(name = "get-channel", description = "Get channel metadata and statistics")
    public String getChannel(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID; omit for own channel)") String channelId) {
        try {
            String resolved = resolveChannelId(channelId);
            Channel channel = resolved != null
                    ? channelService.getChannelById(resolved)
                    : channelService.getMyChannel();

            ChannelSnippet snippet = channel.getSnippet();
            ChannelStatistics stats = channel.getStatistics();

            long subscribers = stats != null && stats.getSubscriberCount() != null
                    ? stats.getSubscriberCount().longValue() : 0;
            long views = stats != null && stats.getViewCount() != null
                    ? stats.getViewCount().longValue() : 0;
            long videos = stats != null && stats.getVideoCount() != null
                    ? stats.getVideoCount().longValue() : 0;

            return String.format("""
                    Channel:
                      ID: %s
                      Title: %s
                      Description: %s
                      Country: %s
                      Language: %s
                      Subscribers: %d
                      Views: %d
                      Videos: %d
                      URL: https://www.youtube.com/channel/%s
                    """,
                    channel.getId(),
                    snippet != null ? snippet.getTitle() : "",
                    snippet != null ? snippet.getDescription() : "",
                    snippet != null ? snippet.getCountry() : "",
                    snippet != null ? snippet.getDefaultLanguage() : "",
                    subscribers,
                    views,
                    videos,
                    channel.getId());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-channel", description = "Update channel description, keywords, country, and default language")
    public String updateChannel(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID)") String channelId,
            @Option(longName = "description", description = "Channel description") String description,
            @Option(longName = "keywords", description = "Space-separated keywords") String keywords,
            @Option(longName = "country", description = "ISO 3166-1 alpha-2 country code (e.g. US)") String country,
            @Option(longName = "language", description = "Default language (e.g. en)") String language) {
        try {
            String resolved = resolveChannelId(channelId);
            if (resolved == null) {
                Channel mine = channelService.getMyChannel();
                resolved = mine.getId();
            }
            channelService.updateChannel(
                    resolved,
                    (description == null || description.isEmpty()) ? null : description,
                    (keywords == null || keywords.isEmpty()) ? null : keywords,
                    (country == null || country.isEmpty()) ? null : country,
                    (language == null || language.isEmpty()) ? null : language);
            return "Channel updated: " + resolved;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "upload-channel-banner", description = "Upload a channel banner image")
    public String uploadChannelBanner(
            @Option(longName = "file", description = "Path to image file (min 2048x1152 px)", required = true) String filePath) {
        try {
            String url = channelService.insertChannelBanner(filePath);
            return String.format("""
                    Channel banner uploaded:
                      URL: %s
                    """, url);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "set-watermark", description = "Set a branding watermark on all channel videos")
    public String setWatermark(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID)") String channelId,
            @Option(longName = "file", description = "Path to watermark image file", required = true) String filePath,
            @Option(longName = "timing-type", description = "offsetFromStart or offsetFromEnd", defaultValue = "offsetFromEnd") String timingType,
            @Option(longName = "offset-ms", description = "Offset in milliseconds from timing type", defaultValue = "15000") Long offsetMs,
            @Option(longName = "duration-ms", description = "Display duration in milliseconds (0 = rest of video)", defaultValue = "0") Long durationMs) {
        try {
            String resolved = resolveChannelId(channelId);
            if (resolved == null) {
                Channel mine = channelService.getMyChannel();
                resolved = mine.getId();
            }
            channelService.setWatermark(resolved, filePath, timingType, offsetMs, durationMs);
            return "Watermark set on channel: " + resolved;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "unset-watermark", description = "Remove branding watermark from channel")
    public String unsetWatermark(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID)") String channelId) {
        try {
            String resolved = resolveChannelId(channelId);
            if (resolved == null) {
                Channel mine = channelService.getMyChannel();
                resolved = mine.getId();
            }
            channelService.unsetWatermark(resolved);
            return "Watermark removed from channel: " + resolved;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-channel-sections", description = "List channel homepage sections")
    public String listChannelSections(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID; omit for own channel)") String channelId) {
        try {
            String resolved = resolveChannelId(channelId);
            List<ChannelSection> sections = channelService.listChannelSections(resolved);

            if (sections.isEmpty()) {
                return "No channel sections found.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Sections (%d):\n", sections.size()));
            for (ChannelSection s : sections) {
                var snippet = s.getSnippet();
                sb.append(String.format("  ID: %s  Type: %s  Style: %s  Title: %s\n",
                        s.getId(),
                        snippet != null ? snippet.getType() : "",
                        snippet != null ? snippet.getStyle() : "",
                        snippet != null ? snippet.getTitle() : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "create-channel-section", description = "Create a channel homepage section")
    public String createChannelSection(
            @Option(longName = "channel-id", description = "Channel ID (or set YTCLI_CHANNEL_ID)") String channelId,
            @Option(longName = "type", description = "Section type (e.g. singlePlaylist, multiplePlaylists, recentUploads)", required = true) String type,
            @Option(longName = "title", description = "Section title") String title,
            @Option(longName = "style", description = "verticalList or verticalGrid", defaultValue = "verticalList") String style) {
        try {
            String resolved = resolveChannelId(channelId);
            ChannelSection section = channelService.insertChannelSection(
                    resolved, type, (title == null || title.isEmpty()) ? null : title, style);
            return String.format("""
                    Channel section created:
                      ID: %s
                      Type: %s
                      Style: %s
                      Title: %s
                    """,
                    section.getId(),
                    section.getSnippet().getType(),
                    section.getSnippet().getStyle(),
                    section.getSnippet().getTitle());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-channel-section", description = "Update a channel section title")
    public String updateChannelSection(
            @Option(longName = "section-id", description = "Channel section ID", required = true) String sectionId,
            @Option(longName = "title", description = "New title", required = true) String title) {
        try {
            ChannelSection updated = channelService.updateChannelSection(sectionId, title);
            return String.format("Channel section updated: %s  Title: %s",
                    updated.getId(), updated.getSnippet().getTitle());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-channel-section", description = "Delete a channel homepage section")
    public String deleteChannelSection(
            @Option(longName = "section-id", description = "Channel section ID", required = true) String sectionId) {
        try {
            channelService.deleteChannelSection(sectionId);
            return "Deleted channel section: " + sectionId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
