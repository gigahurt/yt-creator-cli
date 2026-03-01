package net.gigahurt.ytcreatorcli.command;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import net.gigahurt.ytcreatorcli.model.VideoCategory;
import net.gigahurt.ytcreatorcli.model.VideoDetail;
import net.gigahurt.ytcreatorcli.service.ThumbnailService;
import net.gigahurt.ytcreatorcli.service.YouTubeService;

import java.util.Arrays;
import java.util.List;

@Component
public class VideoCommands {

    private final YouTubeService youTubeService;
    private final ThumbnailService thumbnailService;

    public VideoCommands(YouTubeService youTubeService, ThumbnailService thumbnailService) {
        this.youTubeService = youTubeService;
        this.thumbnailService = thumbnailService;
    }

    @Command(name = "get-video", description = "Get current metadata for a video")
    public String getVideo(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId) {
        try {
            var video = youTubeService.getVideo(videoId);
            VideoDetail detail = youTubeService.toVideoDetail(video);
            return formatVideoDetail(detail);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-video", description = "Update video title, description, tags, visibility, and more")
    public String updateVideo(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId,
            @Option(longName = "title", description = "New title") String title,
            @Option(longName = "description", description = "New description") String description,
            @Option(longName = "tags", description = "Comma-separated tags") String tags,
            @Option(longName = "category-id", description = "Category ID (e.g. 20=Gaming, 24=Entertainment)") String categoryId,
            @Option(longName = "language", description = "Default language for title/description (e.g. en)") String defaultLanguage,
            @Option(longName = "audio-language", description = "Default audio language (e.g. en)") String defaultAudioLanguage,
            @Option(longName = "visibility", description = "public, private, or unlisted") String visibility,
            @Option(longName = "made-for-kids", description = "true or false") String madeForKids,
            @Option(longName = "publish-at", description = "ISO 8601 datetime (e.g. 2026-03-01T15:00:00Z)") String publishAt,
            @Option(longName = "thumbnail", description = "Path to thumbnail image file") String thumbnail) {
        try {
            List<String> tagList = null;
            if (tags != null && !tags.isEmpty()) {
                tagList = Arrays.stream(tags.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            String effTitle = (title == null || title.isEmpty()) ? null : title;
            String effDescription = (description == null || description.isEmpty()) ? null : description;
            String effCategoryId = resolveCategoryId(categoryId);
            String effLanguage = (defaultLanguage == null || defaultLanguage.isEmpty()) ? null : defaultLanguage;
            String effAudioLanguage = (defaultAudioLanguage == null || defaultAudioLanguage.isEmpty()) ? null : defaultAudioLanguage;
            String effVisibility = (visibility == null || visibility.isEmpty()) ? null : visibility;
            String effPublishAt = (publishAt == null || publishAt.isEmpty()) ? null : publishAt;
            Boolean effMadeForKids = (madeForKids == null || madeForKids.isEmpty()) ? null : Boolean.valueOf(madeForKids);

            VideoDetail detail = youTubeService.updateVideo(
                    videoId, effTitle, effDescription, tagList, effCategoryId,
                    effLanguage, effAudioLanguage, effVisibility, effMadeForKids, effPublishAt);

            StringBuilder sb = new StringBuilder();
            sb.append(formatVideoDetail(detail));

            if (thumbnail != null && !thumbnail.isEmpty()) {
                String result = thumbnailService.setThumbnail(videoId, thumbnail);
                sb.append("Thumbnail: ").append(result).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String resolveCategoryId(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        // If it's a numeric ID, use it directly
        if (input.matches("\\d+")) {
            return input;
        }
        // Try to match by enum name (e.g. GAMING, MUSIC)
        try {
            return VideoCategory.valueOf(input.toUpperCase()).getId();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown category: " + input + ". Valid values:\n" + VideoCategory.listAll());
        }
    }

    private String formatCategoryDisplay(String categoryId) {
        if (categoryId == null) return "null";
        VideoCategory cat = VideoCategory.fromId(categoryId);
        return cat != null
                ? String.format("%s (%s)", cat.getDisplayName(), categoryId)
                : categoryId;
    }

    private String formatVideoDetail(VideoDetail detail) {
        return String.format("""
                Video:
                  ID: %s
                  Title: %s
                  Description: %s
                  Tags: %s
                  Category: %s
                  Language: %s
                  Audio language: %s
                  Visibility: %s
                  Made for kids: %s
                  Scheduled publish: %s
                  Published: %s
                """,
                detail.videoId(),
                detail.title(),
                detail.description(),
                detail.tags(),
                formatCategoryDisplay(detail.categoryId()),
                detail.defaultLanguage(),
                detail.defaultAudioLanguage(),
                detail.visibility(),
                detail.madeForKids(),
                detail.publishAt(),
                detail.publishedAt());
    }
}
