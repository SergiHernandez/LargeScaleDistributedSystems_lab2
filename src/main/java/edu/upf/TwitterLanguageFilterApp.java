package edu.upf;

import edu.upf.model.SimplifiedTweet;
//import edu.upf.uploader.S3Uploader;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TwitterLanguageFilterApp {
    public static void main( String[] args ) throws IOException {
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
        
        JavaRDD<SimplifiedTweet> simplifiedTweets = tweets
                                        .map(tweet -> SimplifiedTweet.fromJson(tweet))//convert raw tweet to SimplifiedTweet with optional type
                                        .filter(tweet -> tweet.isPresent())//Check that the Optional SimplifiedTweet is not empty
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> language.equals(tweet.getLanguage())); //We should do it in two steps

        simplifiedTweets.saveAsTextFile(outputFile);

        long stop = System.currentTimeMillis();
        System.out.println("Done in ms: " + (stop - start));
    }
}
