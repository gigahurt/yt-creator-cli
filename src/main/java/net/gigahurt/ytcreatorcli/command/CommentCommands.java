package net.gigahurt.ytcreatorcli.command;

import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentListResponse;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadSnippet;

import net.gigahurt.ytcreatorcli.service.CommentService;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CommentCommands {

    private final CommentService commentService;

    public CommentCommands(CommentService commentService) {
        this.commentService = commentService;
    }

    @Command(name = "list-comments", description = "List top-level comments on a video")
    public String listComments(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId,
            @Option(longName = "order", description = "time or relevance", defaultValue = "time") String order,
            @Option(longName = "max-results", description = "Max results (1-100)", defaultValue = "20") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            CommentThreadListResponse response = commentService.listCommentThreads(
                    videoId, order, maxResults, (pageToken == null || pageToken.isEmpty()) ? null : pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No comments found for video: " + videoId;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Comments on %s:\n", videoId));
            for (CommentThread thread : response.getItems()) {
                CommentThreadSnippet ts = thread.getSnippet();
                Comment top = ts != null ? ts.getTopLevelComment() : null;
                CommentSnippet cs = top != null ? top.getSnippet() : null;
                long likes = cs != null && cs.getLikeCount() != null ? cs.getLikeCount().longValue() : 0;
                long replies = ts != null && ts.getTotalReplyCount() != null ? ts.getTotalReplyCount().longValue() : 0;
                sb.append(String.format("  Thread: %s  By: %s  Likes: %d  Replies: %d\n",
                        thread.getId(),
                        cs != null ? cs.getAuthorDisplayName() : "",
                        likes,
                        replies));
                if (cs != null) {
                    sb.append(String.format("  Text: %s\n", truncate(cs.getTextDisplay(), 200)));
                    sb.append(String.format("  Posted: %s\n",
                            cs.getPublishedAt() != null ? cs.getPublishedAt().toString() : ""));
                }
                sb.append("  ---\n");
            }
            if (response.getNextPageToken() != null) {
                sb.append("Next page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "post-comment", description = "Post a top-level comment on a video")
    public String postComment(
            @Option(longName = "video-id", description = "YouTube video ID", required = true) String videoId,
            @Option(longName = "text", description = "Comment text", required = true) String text) {
        try {
            CommentThread thread = commentService.insertCommentThread(videoId, text);
            Comment top = thread.getSnippet() != null ? thread.getSnippet().getTopLevelComment() : null;
            return String.format("""
                    Comment posted:
                      Thread ID: %s
                      Comment ID: %s
                      Text: %s
                    """,
                    thread.getId(),
                    top != null ? top.getId() : "",
                    text);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "list-replies", description = "List replies to a comment thread")
    public String listReplies(
            @Option(longName = "comment-id", description = "Parent comment/thread ID", required = true) String commentId,
            @Option(longName = "max-results", description = "Max results (1-100)", defaultValue = "20") Long maxResults,
            @Option(longName = "page-token", description = "Pagination token") String pageToken) {
        try {
            CommentListResponse response = commentService.listReplies(
                    commentId, maxResults, (pageToken == null || pageToken.isEmpty()) ? null : pageToken);

            if (response.getItems() == null || response.getItems().isEmpty()) {
                return "No replies found for comment: " + commentId;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Replies to %s:\n", commentId));
            for (Comment c : response.getItems()) {
                CommentSnippet cs = c.getSnippet();
                long likes = cs != null && cs.getLikeCount() != null ? cs.getLikeCount().longValue() : 0;
                sb.append(String.format("  ID: %s  By: %s  Likes: %d\n",
                        c.getId(),
                        cs != null ? cs.getAuthorDisplayName() : "",
                        likes));
                if (cs != null) {
                    sb.append(String.format("  Text: %s\n", truncate(cs.getTextDisplay(), 200)));
                }
                sb.append("  ---\n");
            }
            if (response.getNextPageToken() != null) {
                sb.append("Next page token: ").append(response.getNextPageToken()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "reply-comment", description = "Post a reply to a comment")
    public String replyComment(
            @Option(longName = "comment-id", description = "Parent comment/thread ID to reply to", required = true) String commentId,
            @Option(longName = "text", description = "Reply text", required = true) String text) {
        try {
            Comment reply = commentService.insertReply(commentId, text);
            return String.format("""
                    Reply posted:
                      Comment ID: %s
                      Text: %s
                    """,
                    reply.getId(),
                    text);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "update-comment", description = "Edit your own comment text")
    public String updateComment(
            @Option(longName = "comment-id", description = "Comment ID", required = true) String commentId,
            @Option(longName = "text", description = "New text", required = true) String text) {
        try {
            Comment updated = commentService.updateComment(commentId, text);
            return String.format("Comment updated: %s", updated.getId());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "delete-comment", description = "Delete your own comment")
    public String deleteComment(
            @Option(longName = "comment-id", description = "Comment ID", required = true) String commentId) {
        try {
            commentService.deleteComment(commentId);
            return "Deleted comment: " + commentId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Command(name = "moderate-comment", description = "Set moderation status on one or more comments")
    public String moderateComment(
            @Option(longName = "comment-id", description = "Comment ID (comma-separated for multiple)", required = true) String commentIds,
            @Option(longName = "status", description = "heldForReview, published, or rejected", required = true) String status,
            @Option(longName = "ban-author", description = "Also ban the comment author (true/false)", defaultValue = "false") String banAuthor) {
        try {
            List<String> ids = Arrays.stream(commentIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            commentService.setModerationStatus(ids, status, Boolean.parseBoolean(banAuthor));
            return String.format("Moderation status set to '%s' for %d comment(s).", status, ids.size());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
