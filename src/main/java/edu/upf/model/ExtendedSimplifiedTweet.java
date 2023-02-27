/*
package edu.upf.model;

import java.io.Serializable;
import java.util.Optional;


public class ExtendedSimplifiedTweet implements Serializable {
    private final long tweetId;
    private final String text;
    private final long userId;
    private final String userName;
    private final long followersCount;
    private final String language;
    private final boolean isRetweeted;
    private final Long retweetedUserId;
    private final Long retweetedTweetId;
    private final long timestampMs;

    public ExtendedSimplifiedTweet(long tweetId, String text, long userId, String userName, long followersCount, String language, boolean isRetweeted, Long retweetedUserId, Long retweetedTweetId, long timestampMs) {
    // IMPLEMENT ME
        this.tweetId = tweetId;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.followersCount = followersCount;
        this.language = language;
    }
    /**
    * Returns a {@link ExtendedSimplifiedTweet} from a JSON String.
    * If parsing fails, for any reason, return an {@link 
        
        #empty()}
    *
    * @param jsonStr
    * @return an {@link Optional} of a {@link ExtendedSimplifiedTweet}
    */
    /*
    public static Optional<ExtendedSimplifiedTweet> fromJson(String jsonStr) {
    // IMPLEMENT ME
        int a;
    }
}
*/