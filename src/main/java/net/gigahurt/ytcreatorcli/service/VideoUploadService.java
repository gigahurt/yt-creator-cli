package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoGetRatingResponse;
import com.google.api.services.youtube.model.VideoRating;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class VideoUploadService {

    private final YouTube youtube;

    public VideoUploadService(YouTube youtube) {
        this.youtube = youtube;
    }

    public Video uploadVideo(String filePath, String title, String description,
                              List<String> tags, String categoryId, String visibility,
                              Boolean madeForKids, String language) throws IOException {
        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            throw new IllegalArgumentException("Video file not found: " + filePath);
        }

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        if (description != null && !description.isBlank()) {
            snippet.setDescription(description);
        }
        if (tags != null && !tags.isEmpty()) {
            snippet.setTags(tags);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            snippet.setCategoryId(categoryId);
        }
        if (language != null && !language.isBlank()) {
            snippet.setDefaultLanguage(language);
            snippet.setDefaultAudioLanguage(language);
        }

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(visibility != null ? visibility : "private");
        if (madeForKids != null) {
            status.setSelfDeclaredMadeForKids(madeForKids);
        }

        Video video = new Video();
        video.setSnippet(snippet);
        video.setStatus(status);

        String mimeType = determineMimeType(filePath);
        FileContent mediaContent = new FileContent(mimeType, videoFile);

        YouTube.Videos.Insert request = youtube.videos()
                .insert(List.of("snippet", "status"), video, mediaContent);
        request.getMediaHttpUploader().setDirectUploadEnabled(false);
        request.getMediaHttpUploader().setProgressListener(uploader -> {
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED -> System.err.println("Upload starting...");
                case MEDIA_IN_PROGRESS ->
                    System.err.printf("Upload: %.0f%%%n", uploader.getProgress() * 100);
                case MEDIA_COMPLETE -> System.err.println("Upload complete.");
                default -> { }
            }
        });

        return request.execute();
    }

    public void deleteVideo(String videoId) throws IOException {
        youtube.videos().delete(videoId).execute();
    }

    public void rateVideo(String videoId, String rating) throws IOException {
        youtube.videos().rate(videoId, rating).execute();
    }

    public String getVideoRating(String videoId) throws IOException {
        VideoGetRatingResponse response = youtube.videos()
                .getRating(List.of(videoId))
                .execute();
        if (response.getItems() == null || response.getItems().isEmpty()) {
            return "none";
        }
        VideoRating rating = response.getItems().getFirst();
        return rating.getRating() != null ? rating.getRating() : "none";
    }

    private String determineMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".avi")) return "video/avi";
        if (lower.endsWith(".mkv")) return "video/x-matroska";
        if (lower.endsWith(".webm")) return "video/webm";
        return "video/mp4";
    }
}
