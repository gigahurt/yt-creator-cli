package net.gigahurt.ytcreatorcli.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.ThumbnailSetResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ThumbnailService {

    private final YouTube youtube;

    public ThumbnailService(YouTube youtube) {
        this.youtube = youtube;
    }

    public String setThumbnail(String videoId, String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Thumbnail file not found: " + imagePath);
        }

        String mimeType = determineMimeType(imagePath);
        FileContent mediaContent = new FileContent(mimeType, imageFile);

        ThumbnailSetResponse response = youtube.thumbnails()
                .set(videoId, mediaContent)
                .execute();

        if (response.getItems() != null && !response.getItems().isEmpty()) {
            ThumbnailDetails details = response.getItems().getFirst();
            if (details.getDefault() != null) {
                return details.getDefault().getUrl();
            }
            if (details.getHigh() != null) {
                return details.getHigh().getUrl();
            }
        }
        return "Thumbnail set successfully";
    }

    private String determineMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "image/png";
    }
}
