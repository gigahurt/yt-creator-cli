package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionSnippet;

import net.gigahurt.ytcreatorcli.service.CaptionService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CaptionCommands {

    private final CaptionService captionService;

    public CaptionCommands(CaptionService captionService) {
        this.captionService = captionService;
    }

    @Command(name = "list-captions", description = "List caption tracks for a video")
    public String listCaptions(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId) {
        try {
            List<Caption> captions = captionService.listCaptions(videoId);
            if (captions.isEmpty()) {
                return "No caption tracks found for video: " + videoId;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Captions for video %s:\n", videoId));
            for (Caption c : captions) {
                CaptionSnippet s = c.getSnippet();
                sb.append(String.format("  ID: %s  Language: %s  Name: %s  Kind: %s  Status: %s  Draft: %s\n",
                        c.getId(),
                        s != null ? s.getLanguage() : "",
                        s != null ? s.getName() : "",
                        s != null ? s.getTrackKind() : "",
                        s != null ? s.getStatus() : "",
                        s != null ? s.getIsDraft() : ""));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "upload-caption", description = "Upload a new caption track to a video")
    public String uploadCaption(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId,
            @Option(longName = "language", description = "BCP-47 language code (e.g. en, fr, ja)", required = true) String language,
            @Option(longName = "name", description = "Caption track name") String name,
            @Option(longName = "file", description = "Path to .srt, .vtt, or .ttml file", required = true) String filePath,
            @Option(longName = "draft", description = "Upload as draft (true/false)", defaultValue = "false") String isDraft) {
        try {
            Caption caption = captionService.insertCaption(
                    videoId, language, (name == null || name.isEmpty()) ? null : name,
                    Boolean.parseBoolean(isDraft), filePath);
            CaptionSnippet s = caption.getSnippet();
            return String.format("""
                    Caption uploaded:
                      ID: %s
                      Language: %s
                      Name: %s
                      Status: %s
                    """,
                    caption.getId(),
                    s != null ? s.getLanguage() : "",
                    s != null ? s.getName() : "",
                    s != null ? s.getStatus() : "");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "download-caption", description = "Download a caption track to a file")
    public String downloadCaption(
            @Option(longName = "caption-id", description = "Caption track ID", required = true) String captionId,
            @Option(longName = "output", description = "Output file path", required = true) String outputPath,
            @Option(longName = "format", description = "srt, vtt, ttml, or dfxp", defaultValue = "srt") String format) {
        try {
            String saved = captionService.downloadCaption(captionId, outputPath, format);
            return "Caption saved to " + saved;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-caption", description = "Update a caption track name, draft status, or replace content")
    public String updateCaption(
            @Option(longName = "caption-id", description = "Caption track ID", required = true) String captionId,
            @Option(longName = "name", description = "New track name") String name,
            @Option(longName = "draft", description = "Set draft status (true/false)") String isDraft,
            @Option(longName = "file", description = "Path to replacement caption file") String filePath) {
        try {
            Boolean effDraft = (isDraft == null || isDraft.isEmpty()) ? null : Boolean.parseBoolean(isDraft);
            Caption updated = captionService.updateCaption(
                    captionId,
                    (name == null || name.isEmpty()) ? null : name,
                    effDraft,
                    (filePath == null || filePath.isEmpty()) ? null : filePath);
            CaptionSnippet s = updated.getSnippet();
            return String.format("""
                    Caption updated:
                      ID: %s
                      Language: %s
                      Name: %s
                      Draft: %s
                    """,
                    updated.getId(),
                    s != null ? s.getLanguage() : "",
                    s != null ? s.getName() : "",
                    s != null ? s.getIsDraft() : "");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-caption", description = "Delete a caption track")
    public String deleteCaption(
            @Option(longName = "caption-id", description = "Caption track ID", required = true) String captionId) {
        try {
            captionService.deleteCaption(captionId);
            return "Deleted caption track: " + captionId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
