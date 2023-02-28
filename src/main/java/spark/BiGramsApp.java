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

public class BiGramsApp {
    public static void main( String[] args ) throws IOException {
        //Arguments: language, input files, output files
        List<String> argsList = Arrays.asList(args);
        String language = argsList.get(0);
        String outputFile = argsList.get(1);
        System.out.println("\n\nLanguage: " + language + ". Output folder: " + outputFile + "\n\n");

        //Start Spark context
        SparkConf conf = new SparkConf().setAppName("TwitterLanguageFilterApp");
        JavaSparkContext sc = new JavaSparkContext(conf);

        System.out.println("\n\nWe created Spark Context\n\n");

        long start = System.currentTimeMillis();

        JavaRDD<String> tweets = sc.textFile(argsList.get(2)); 
        
        long TweeeetsCount = tweets.count();
        System.out.println("\n\nTotal number of tweets: " + TweeeetsCount + "\n\n"); // Debugging

        JavaRDD<ExtendedSimplifiedTweet> simplifiedTweets = tweets
                                        .map(tweet -> ExtendedSimplifiedTweet.fromJson(tweet))//convert raw tweet to SimplifiedTweet with optional type
                                        .filter(tweet -> tweet.isPresent())//Check that the Optional SimplifiedTweet is not empty
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> !tweet.isRetweeted())
                                        .filter(tweet -> language.equals(tweet.getLanguage())); //We should do it in two steps

        JavaRDD<String> tweetsText = simplifiedTweets
                                        .map(simplifiedTweet->simplifiedTweet.getText());

        JavaPairRDD<String, Integer> bigrams = tweetsText
                                        .flatMap(tweetText -> bigram(tweetText).iterator())
                                        .mapToPair(bigram -> new Tuple2<>(bigram, 1))
                                        .reduceByKey((a, b) -> a + b);

        JavaPairRDD<Integer, String> bigramsSorted = bigrams
                                        .flatMapToPair(item -> Collections.singletonList(item.swap()).iterator())
                                        .sortByKey(false);

        List<Tuple2<Integer, String>> top10bigramsTuples = bigramsSorted
                                        .take(10);
        
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Check why we have bigrams with only one word (empty words)!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        JavaPairRDD<Integer, String> top10bigrams = sc.parallelizePairs(top10bigramsTuples);
        
        top10bigrams.saveAsTextFile(outputFile);

        long stop = System.currentTimeMillis();
        System.out.println("Done in ms: " + (stop - start));
        //
    }

    public static List<String> bigram (String s) {
        List<String> bigrams = new ArrayList<String>();
        String[] words = s.split("[\\p{Punct}\\s]+");
        if (words.length >= 2) { //Checking that the tweet has at least 2 words
            for (int i = 0; i < words.length - 1; i++) {
                if (!words[i].equals("") && !words[i+1].equals("")) {
                    bigrams.add(words[i]+" "+words[i+1]);
                }
            }   
        }
        return bigrams;
    }
}
