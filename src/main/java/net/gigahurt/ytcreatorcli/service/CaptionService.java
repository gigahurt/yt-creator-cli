package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionSnippet;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class CaptionService {

    private final YouTube youtube;

    public CaptionService(YouTube youtube) {
        this.youtube = youtube;
    }

    public List<Caption> listCaptions(String videoId) throws IOException {
        var response = youtube.captions()
                .list(List.of("snippet"), videoId)
                .execute();
        return response.getItems() != null ? response.getItems() : List.of();
    }

    public Caption insertCaption(String videoId, String language, String name,
                                  boolean isDraft, String filePath) throws IOException {
        File captionFile = new File(filePath);
        if (!captionFile.exists()) {
            throw new IllegalArgumentException("Caption file not found: " + filePath);
        }

        CaptionSnippet snippet = new CaptionSnippet();
        snippet.setVideoId(videoId);
        snippet.setLanguage(language);
        snippet.setIsDraft(isDraft);
        if (name != null && !name.isBlank()) {
            snippet.setName(name);
        }

        Caption caption = new Caption();
        caption.setSnippet(snippet);

        String mimeType = determineMimeType(filePath);
        FileContent mediaContent = new FileContent(mimeType, captionFile);

        return youtube.captions()
                .insert(List.of("snippet"), caption, mediaContent)
                .execute();
    }

    public String downloadCaption(String captionId, String outputPath, String format) throws IOException {
        YouTube.Captions.Download request = youtube.captions().download(captionId);
        if (format != null && !format.isBlank()) {
            request.setTfmt(format);
        }

        try (OutputStream out = new FileOutputStream(outputPath)) {
            request.executeMediaAndDownloadTo(out);
        }
        return outputPath;
    }

    public Caption updateCaption(String captionId, String name, Boolean isDraft,
                                  String filePath) throws IOException {
        var existing = youtube.captions()
                .list(List.of("snippet"), "")
                .setId(List.of(captionId))
                .execute();
        if (existing.getItems() == null || existing.getItems().isEmpty()) {
            throw new IllegalArgumentException("Caption not found: " + captionId);
        }
        Caption caption = existing.getItems().getFirst();

        if (name != null && !name.isBlank()) {
            caption.getSnippet().setName(name);
        }
        if (isDraft != null) {
            caption.getSnippet().setIsDraft(isDraft);
        }

        if (filePath != null && !filePath.isBlank()) {
            File captionFile = new File(filePath);
            if (!captionFile.exists()) {
                throw new IllegalArgumentException("Caption file not found: " + filePath);
            }
            String mimeType = determineMimeType(filePath);
            FileContent mediaContent = new FileContent(mimeType, captionFile);
            return youtube.captions()
                    .update(List.of("snippet"), caption, mediaContent)
                    .execute();
        }

        return youtube.captions()
                .update(List.of("snippet"), caption)
                .execute();
    }

    public void deleteCaption(String captionId) throws IOException {
        youtube.captions().delete(captionId).execute();
    }

    private String determineMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".vtt")) return "text/vtt";
        if (lower.endsWith(".ttml") || lower.endsWith(".dfxp")) return "application/ttml+xml";
        return "text/plain"; // .srt and default
    }
}
