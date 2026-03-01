package net.gigahurt.ytcreatorcli.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentListResponse;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CommentService {

    private final YouTube youtube;

    public CommentService(YouTube youtube) {
        this.youtube = youtube;
    }

    public CommentThreadListResponse listCommentThreads(String videoId, String order,
                                                         long maxResults,
                                                         String pageToken) throws IOException {
        YouTube.CommentThreads.List request = youtube.commentThreads()
                .list(List.of("snippet", "replies"))
                .setVideoId(videoId)
                .setOrder(order != null ? order : "time")
                .setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public CommentThread insertCommentThread(String videoId, String text) throws IOException {
        CommentSnippet topSnippet = new CommentSnippet();
        topSnippet.setTextOriginal(text);

        Comment topComment = new Comment();
        topComment.setSnippet(topSnippet);

        CommentThreadSnippet snippet = new CommentThreadSnippet();
        snippet.setVideoId(videoId);
        snippet.setTopLevelComment(topComment);

        CommentThread thread = new CommentThread();
        thread.setSnippet(snippet);

        return youtube.commentThreads()
                .insert(List.of("snippet"), thread)
                .execute();
    }

    public CommentListResponse listReplies(String parentId, long maxResults,
                                            String pageToken) throws IOException {
        YouTube.Comments.List request = youtube.comments()
                .list(List.of("snippet"))
                .setParentId(parentId)
                .setMaxResults(maxResults);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        return request.execute();
    }

    public Comment insertReply(String parentId, String text) throws IOException {
        CommentSnippet snippet = new CommentSnippet();
        snippet.setTextOriginal(text);
        snippet.setParentId(parentId);

        Comment comment = new Comment();
        comment.setSnippet(snippet);

        return youtube.comments()
                .insert(List.of("snippet"), comment)
                .execute();
    }

    public Comment updateComment(String commentId, String newText) throws IOException {
        CommentSnippet snippet = new CommentSnippet();
        snippet.setTextOriginal(newText);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setSnippet(snippet);

        return youtube.comments()
                .update(List.of("snippet"), comment)
                .execute();
    }

    public void deleteComment(String commentId) throws IOException {
        youtube.comments().delete(commentId).execute();
    }

    public void setModerationStatus(List<String> commentIds, String moderationStatus,
                                     boolean banAuthor) throws IOException {
        youtube.comments()
                .setModerationStatus(commentIds, moderationStatus)
                .setBanAuthor(banAuthor)
                .execute();
    }
}
