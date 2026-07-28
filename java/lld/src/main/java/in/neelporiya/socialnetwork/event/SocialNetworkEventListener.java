package in.neelporiya.socialnetwork.event;

import in.neelporiya.socialnetwork.model.Comment;
import in.neelporiya.socialnetwork.model.FriendRequest;
import in.neelporiya.socialnetwork.model.Post;
import in.neelporiya.socialnetwork.model.User;

/**
 * // DESIGN PATTERN: Observer — actions publish domain events and listeners decide how to notify
 * users. The service does not know whether delivery is push, email, SMS, or just an in-memory list.
 */
public interface SocialNetworkEventListener {

    void onFriendRequestSent(FriendRequest request);

    void onPostLiked(Post post, User likedBy);

    void onCommentAdded(Post post, Comment comment);
}
