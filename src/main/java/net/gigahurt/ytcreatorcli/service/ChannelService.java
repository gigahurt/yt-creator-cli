package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelBannerResource;
import com.google.api.services.youtube.model.ChannelBrandingSettings;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.ChannelSection;
import com.google.api.services.youtube.model.ChannelSectionSnippet;
import com.google.api.services.youtube.model.ChannelSettings;
import com.google.api.services.youtube.model.InvideoBranding;
import com.google.api.services.youtube.model.InvideoTiming;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Service
public class ChannelService {

    private final YouTube youtube;

    public ChannelService(YouTube youtube) {
        this.youtube = youtube;
    }

    public Channel getMyChannel() throws IOException {
        ChannelListResponse response = youtube.channels()
                .list(List.of("snippet", "brandingSettings", "statistics", "contentDetails"))
                .setMine(true)
                .execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new IllegalArgumentException("No channel found for the authenticated user.");
        }
        return response.getItems().getFirst();
    }

    public Channel getChannelById(String channelId) throws IOException {
        ChannelListResponse response = youtube.channels()
                .list(List.of("snippet", "brandingSettings", "statistics", "contentDetails"))
                .setId(List.of(channelId))
                .execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Channel not found: " + channelId);
        }
        return response.getItems().getFirst();
    }

    public Channel updateChannel(String channelId, String description, String keywords,
                                  String country, String defaultLanguage) throws IOException {
        Channel channel = getChannelById(channelId);

        ChannelBrandingSettings branding = channel.getBrandingSettings();
        if (branding == null) {
            branding = new ChannelBrandingSettings();
            channel.setBrandingSettings(branding);
        }
        ChannelSettings settings = branding.getChannel();
        if (settings == null) {
            settings = new ChannelSettings();
            branding.setChannel(settings);
        }

        if (description != null && !description.isBlank()) {
            settings.setDescription(description);
        }
        if (keywords != null && !keywords.isBlank()) {
            settings.setKeywords(keywords);
        }
        if (country != null && !country.isBlank()) {
            settings.setCountry(country);
        }
        if (defaultLanguage != null && !defaultLanguage.isBlank()) {
            settings.setDefaultLanguage(defaultLanguage);
        }

        return youtube.channels()
                .update(List.of("brandingSettings"), channel)
                .execute();
    }

    public String insertChannelBanner(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Image file not found: " + imagePath);
        }

        String mimeType = imagePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        FileContent mediaContent = new FileContent(mimeType, imageFile);

        ChannelBannerResource banner = new ChannelBannerResource();
        ChannelBannerResource result = youtube.channelBanners()
                .insert(banner, mediaContent)
                .execute();
        return result.getUrl() != null ? result.getUrl() : "Banner uploaded successfully";
    }

    public void setWatermark(String channelId, String imagePath, String timingType,
                              Long offsetMs, Long durationMs) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Image file not found: " + imagePath);
        }

        InvideoBranding branding = new InvideoBranding();

        InvideoTiming timing = new InvideoTiming();
        timing.setType(timingType != null ? timingType : "offsetFromEnd");
        timing.setOffsetMs(BigInteger.valueOf(offsetMs != null ? offsetMs : 15000L));
        if (durationMs != null && durationMs > 0) {
            timing.setDurationMs(BigInteger.valueOf(durationMs));
        }
        branding.setTiming(timing);

        String mimeType = imagePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        FileContent mediaContent = new FileContent(mimeType, imageFile);

        youtube.watermarks()
                .set(channelId, branding, mediaContent)
                .execute();
    }

    public void unsetWatermark(String channelId) throws IOException {
        youtube.watermarks().unset(channelId).execute();
    }

    public List<ChannelSection> listChannelSections(String channelId) throws IOException {
        YouTube.ChannelSections.List request = youtube.channelSections()
                .list(List.of("snippet", "contentDetails"));
        if (channelId != null && !channelId.isBlank()) {
            request.setChannelId(channelId);
        } else {
            request.setMine(true);
        }
        var response = request.execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public ChannelSection insertChannelSection(String channelId, String type, String title,
                                                String style) throws IOException {
        ChannelSectionSnippet snippet = new ChannelSectionSnippet();
        snippet.setType(type);
        snippet.setStyle(style != null ? style : "verticalList");
        if (title != null && !title.isBlank()) {
            snippet.setTitle(title);
        }
        if (channelId != null && !channelId.isBlank()) {
            snippet.setChannelId(channelId);
        }

        ChannelSection section = new ChannelSection();
        section.setSnippet(snippet);

        return youtube.channelSections()
                .insert(List.of("snippet"), section)
                .execute();
    }

    public ChannelSection updateChannelSection(String sectionId, String title) throws IOException {
        var response = youtube.channelSections()
                .list(List.of("snippet", "contentDetails"))
                .setId(List.of(sectionId))
                .execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new IllegalArgumentException("Channel section not found: " + sectionId);
        }
        ChannelSection section = response.getItems().getFirst();
        section.getSnippet().setTitle(title);

        return youtube.channelSections()
                .update(List.of("snippet"), section)
                .execute();
    }

    public void deleteChannelSection(String sectionId) throws IOException {
        youtube.channelSections().delete(sectionId).execute();
    }
}
