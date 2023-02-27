package edu.upf.model;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class SimplifiedTweet {

  // All classes use the same instance

  private final long tweetId;			  // the id of the tweet ('id')
  private final String text;  		      // the content of the tweet ('text')
  private final long userId;			  // the user id ('user->id')
  private final String userName;		  // the user name ('user'->'name')
  private final String language;          // the language of a tweet ('lang')
  private final long timestampMs;		  // seconduserIds from epoch ('timestamp_ms')

  public SimplifiedTweet(long tweetId, String text, long userId, String userName, String language, long timestampMs) {
    this.tweetId = tweetId;
    this.text = text;
    this.userId = userId;
    this.userName = userName;
    this.language = language;
    this.timestampMs = timestampMs;
  }

  /**
   * Returns a {@link SimplifiedTweet} from a JSON String.
   * If parsing fails, for any reason, return an {@link Optional#empty()}
   *
   * @param jsonStr
   * @return an {@link Optional} of a {@link SimplifiedTweet}
   */
  public static Optional<SimplifiedTweet> fromJson(String jsonStr) {

    try{
      JsonObject jsonObject = new Gson().fromJson(jsonStr, JsonObject.class);

      long tweetId = jsonObject.get("id").getAsLong();
      String text = jsonObject.get("text").getAsString();
      JsonObject user = jsonObject.getAsJsonObject("user");
      long userId = user.get("id").getAsLong();
      String userName = user.get("name").getAsString();
      String language = jsonObject.get("lang").getAsString();
      long timestampMs = jsonObject.get("timestamp_ms").getAsLong();

      SimplifiedTweet simplifiedTweet = new SimplifiedTweet(tweetId, text, userId, userName, language, timestampMs);

      return Optional.of(simplifiedTweet);
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

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
