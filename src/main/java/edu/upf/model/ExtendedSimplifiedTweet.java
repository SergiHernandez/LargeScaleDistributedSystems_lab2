package edu.upf.model;

import java.io.Serializable;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
        this.isRetweeted = isRetweeted;
        this.retweetedUserId = retweetedUserId;
        this.retweetedTweetId = retweetedTweetId;
        this.timestampMs = timestampMs;
    }
    /**
    * Returns a {@link ExtendedSimplifiedTweet} from a JSON String.
    * If parsing fails, for any reason, return an {@link 
        
        #empty()}
    *
    * @param jsonStr
    * @return an {@link Optional} of a {@link ExtendedSimplifiedTweet}
    */

    public static Optional<ExtendedSimplifiedTweet> fromJson(String jsonStr) {
    // IMPLEMENT ME
        try{
            JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);
    
            long tweetId = jsonObject.get("id").getAsLong();
            String text = jsonObject.get("text").getAsString().trim().toLowerCase();
            JsonObject user = jsonObject.getAsJsonObject("user");
            long userId = user.get("id").getAsLong();
            String userName = user.get("name").getAsString();
            long followersCount = user.get("followers_count").getAsLong();
            String language = jsonObject.get("lang").getAsString();
            boolean isRetweeted = false;
            long retweetedUserId = 0;
            long retweetedTweetId = 0;
            try{
                JsonObject retweetedStatus = jsonObject.getAsJsonObject("retweeted_status");
                retweetedUserId = retweetedStatus.getAsJsonObject("user").get("id").getAsLong();
                retweetedTweetId = retweetedStatus.get("id").getAsLong();
                isRetweeted = true;
            }
            catch (Exception e){
                isRetweeted = false;
                retweetedUserId = 0;
                retweetedTweetId = 0;
            }
            long timestampMs = jsonObject.get("timestamp_ms").getAsLong();
    
            ExtendedSimplifiedTweet extendedSimplifiedTweet = new ExtendedSimplifiedTweet(tweetId, text, userId, userName, followersCount, language, isRetweeted, retweetedUserId, retweetedTweetId, timestampMs);
    
            return Optional.of(extendedSimplifiedTweet);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }
    public String getLanguage (){
        return this.language;
    }
    
    public String getText (){
        return this.text;
    }

    public boolean isRetweeted (){
        return this.isRetweeted;
    }
}