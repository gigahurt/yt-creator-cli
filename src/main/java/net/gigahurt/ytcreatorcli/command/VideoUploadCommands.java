package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Video;

import net.gigahurt.ytcreatorcli.model.VideoCategory;
import net.gigahurt.ytcreatorcli.service.VideoUploadService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class VideoUploadCommands {

    private final VideoUploadService videoUploadService;

    public VideoUploadCommands(VideoUploadService videoUploadService) {
        this.videoUploadService = videoUploadService;
    }

    @Command(name = "upload-video", description = "Upload a video file to YouTube")
    public String uploadVideo(
            @Option(longName = "file", description = "Path to video file", required = true) String filePath,
            @Option(longName = "title", description = "Video title", required = true) String title,
            @Option(longName = "description", description = "Video description") String description,
            @Option(longName = "tags", description = "Comma-separated tags") String tags,
            @Option(longName = "category-id", description = "Category ID or name (e.g. GAMING, 20)") String categoryId,
            @Option(longName = "visibility", description = "public, private, or unlisted", defaultValue = "private") String visibility,
            @Option(longName = "made-for-kids", description = "true or false", defaultValue = "false") String madeForKids,
            @Option(longName = "language", description = "Default language (e.g. en)") String language) {
        try {
            List<String> tagList = null;
            if (tags != null && !tags.isEmpty()) {
                tagList = Arrays.stream(tags.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            String effCategoryId = resolveCategoryId(categoryId);
            String effDescription = (description == null || description.isEmpty()) ? null : description;
            String effLanguage = (language == null || language.isEmpty()) ? null : language;
            Boolean effMadeForKids = Boolean.valueOf(madeForKids);

            Video video = videoUploadService.uploadVideo(
                    filePath, title, effDescription, tagList,
                    effCategoryId, visibility, effMadeForKids, effLanguage);

            return String.format("""
                    Uploaded video:
                      ID: %s
                      Title: %s
                      URL: https://www.youtube.com/watch?v=%s
                      Visibility: %s
                    """,
                    video.getId(),
                    video.getSnippet().getTitle(),
                    video.getId(),
                    video.getStatus().getPrivacyStatus());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-video", description = "Permanently delete a video")
    public String deleteVideo(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId) {
        try {
            videoUploadService.deleteVideo(videoId);
            return "Deleted video: " + videoId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "rate-video", description = "Rate a video (like/dislike/none)")
    public String rateVideo(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId,
            @Option(longName = "rating", description = "like, dislike, or none", required = true) String rating) {
        try {
            videoUploadService.rateVideo(videoId, rating);
            return String.format("Rated video %s: %s", videoId, rating);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "get-video-rating", description = "Get your rating on a video")
    public String getVideoRating(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId) {
        try {
            String rating = videoUploadService.getVideoRating(videoId);
            return String.format("Rating for %s: %s", videoId, rating);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String resolveCategoryId(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        if (input.matches("\\d+")) {
            return input;
        }
        try {
            return VideoCategory.valueOf(input.toUpperCase()).getId();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown category: " + input + ". Valid values:\n" + VideoCategory.listAll());
        }
    }
}
