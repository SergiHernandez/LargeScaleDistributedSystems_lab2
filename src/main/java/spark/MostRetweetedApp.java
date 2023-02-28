package spark;

import edu.upf.model.ExtendedSimplifiedTweet;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MostRetweetedApp {

    public static void main( String[] args ) throws IOException {

        //Arguments: language, input files, output files
        List<String> argsList = Arrays.asList(args);
        String outputFile = argsList.get(0);

        //Start Spark context
        SparkConf conf = new SparkConf().setAppName("TwitterLanguageFilterApp");
        JavaSparkContext sc = new JavaSparkContext(conf);

        System.out.println("\n\nWe created Spark Context\n\n");

        JavaRDD<String> tweets = sc.textFile(argsList.get(1)); 

        //Find the most retweeted users -> (userId, count) top 10 sorted
        JavaPairRDD<Integer, String> retweetedUsersSorted = tweets
                                        .map(tweet -> ExtendedSimplifiedTweet.fromJson(tweet))
                                        .filter(tweet -> tweet.isPresent())
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> tweet.isRetweeted())
                                        .map(tweet -> Long.toString(tweet.getRetweetedUserId()))
                                        .mapToPair(retweetedUserId -> new Tuple2<>(retweetedUserId, 1))
                                        .reduceByKey((a, b) -> a + b)
                                        .flatMapToPair(item -> Collections.singletonList(item.swap()).iterator())
                                        .sortByKey(false);

        List<Tuple2<Integer, String>> top10RetweetedUsersSortedTuples = retweetedUsersSorted.take(10);

        //Only retrieve the Ids of the top retweeted users
        JavaRDD<String> top10retweetedUsersSorted = sc.parallelizePairs(top10RetweetedUsersSortedTuples)
                                        .flatMapToPair(item -> Collections.singletonList(item.swap()).iterator())
                                        .keys();

        List<String> top10users = top10retweetedUsersSorted.take(10);
        
        //Original tweet Id, original user Id
        JavaPairRDD<String, String> tweetIDuserID = tweets
                                        .map(tweet -> ExtendedSimplifiedTweet.fromJson(tweet))
                                        .filter(tweet -> tweet.isPresent())
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> tweet.isRetweeted())//is it a retweet
                                        .mapToPair(tweet -> new Tuple2<>(Long.toString(tweet.getRetweetedTweetId()), Long.toString((tweet.getRetweetedUserId()))))
                                        .distinct();//OG tweet - OG user


        //OG user - (OG tweet, count)

        //Find the most retweeted tweets -> (retweetTweetId, count) sorted
        JavaPairRDD<String, Integer> retweetedTweetsSorted = tweets
                                        .map(tweet -> ExtendedSimplifiedTweet.fromJson(tweet))
                                        .filter(tweet -> tweet.isPresent())
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> tweet.isRetweeted())
                                        .map(tweet -> Long.toString(tweet.getRetweetedTweetId())) //Get original, get original user, 1
                                        .mapToPair(retweetedTweetId -> new Tuple2<>(retweetedTweetId, 1))
                                        .reduceByKey((a, b) -> a + b)
                                        .flatMapToPair(item -> Collections.singletonList(item.swap()).iterator())
                                        .sortByKey(false)
                                        .flatMapToPair(item -> Collections.singletonList(item.swap()).iterator());

        //tweetID' - count  JOIN  tweetID - userID
        JavaPairRDD<String, Tuple2<Integer, String>> retweetedTweetsCountUsers = retweetedTweetsSorted.join(tweetIDuserID);

        //Tweets of the tweets of the top 10 retweeted users with the retweet count
        JavaPairRDD<String, Tuple2<Integer, String>> papa = retweetedTweetsCountUsers
                                                            .filter(tweet -> top10users.contains(tweet._2._2));

        //userID - (tweetID, count)
        JavaPairRDD<String, Tuple2<String, Integer>> papa2 = papa
                                                            .mapToPair(inputTuple -> { String tweetID = inputTuple._1();
                                                                                        Integer count = inputTuple._2()._1();
                                                                                        String userID = inputTuple._2()._2();
                                                                                        return new Tuple2<>(userID, new Tuple2<>(tweetID, count));
                                                                                    });

        //reduce by key (a, b) -> max(a,b)
        // userID - (tweetID, count)

        JavaPairRDD<String, Tuple2<String, Integer>> papa3 = papa2
                                                            .reduceByKey((a, b) -> maxTweet(a, b));

        papa3.saveAsTextFile(outputFile);

        //Use both parts to produce final answer -_
        //Final output: top 10 retweeted users with their most retweeted tweet

        //Nota mental retweetedUserId es una String!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        //1. Top10Users' -> List[]

        //2. OG_tweetID' - OG userID
        
        //3. OG_tweetID' - count

        //4. OG_tweetID' - (count, OG userID)

        //5. OG_tweetId' - (OG topUserID, count) .filter(top10retweetedUsersSorted.contains(OG topUserID))

        // OG tweet amb mes count (que el seu OG user estigui al Top10)
    }

    public static Tuple2<String, Integer> maxTweet (Tuple2<String, Integer> a, Tuple2<String, Integer> b){
        if (a._2 > b._2)
            return a;
        else return b;
    }
}
