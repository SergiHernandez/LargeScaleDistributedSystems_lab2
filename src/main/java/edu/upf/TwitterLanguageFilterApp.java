package edu.upf;

import edu.upf.model.SimplifiedTweet;
//import edu.upf.uploader.S3Uploader;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import scala.Tuple2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
//Testing once again...

public class TwitterLanguageFilterApp {
    public static void main( String[] args ) throws IOException {
        List<String> argsList = Arrays.asList(args);
        String language = argsList.get(0);
        String outputFile = argsList.get(1);
        String bucket = argsList.get(2);
        System.out.println("\n\nLanguage: " + language + ". Input file: " + inputFile + ". Output file: " + outputFile + ". Destination bucket: " + bucket+"\n\n");
        
        for(String inputFile: argsList.subList(3, argsList.size())) {
            System.out.println("Processing: " + inputFile);
            final FileLanguageFilter filter = new FileLanguageFilter(inputFile, outputFile);
            filter.filterLanguage(language);
            counter = counter + filter.getCounter();
        }  

        int counter = 0;

        //Start Spark context
        SparkConf conf = new SparkConf().setAppName("TwitterLanguageFilterApp");
        JavaSparkContext sc = new JavaSparkContext(conf);

        System.out.println("\n\nWe created Spark Context\n\n");

        long start = System.currentTimeMillis();

        
        JavaRDD<String> tweets = sc.textFile(argsList.get(3));
        for(String inpFile: argsList.subList(4, argsList.size())) {
            System.out.println("Processing: " + inpFile);
            JavaRDD<String> tweetsAux = sc.textFile(inpFile);
            tweets = tweets.union(tweetsAux);
        }
        
        long TweeeetsCount = tweets.count();
        System.out.println("Number of tweets: " + TweeeetsCount);
        //JavaRDD<String> filteredTweets = tweets.filter(tweet -> );
        //SimplifiedTweet(tweetId, text, userId, userName, language, timestampMs)
        
        JavaRDD<String> filteredTweets = tweets
                                        .flatMap(tweet_page -> Arrays.asList(tweet_page.split("\\{\\}")).iterator());//Read raw tweet (line)

        JavaRDD<SimplifiedTweet> simplifiedTweets = filteredTweets
                                        .map(tweet -> SimplifiedTweet.fromJson(tweet))//convert raw tweet to SimplifiedTweet with optional type
                                        .filter(tweet -> tweet.isPresent())//Check that the Optional SimplifiedTweet is not empty
                                        .map(tweet -> tweet.get())
                                        .filter(tweet -> language.equals(tweet.getLanguage())); //We should do it in two steps
        
        long TweetsCount = simplifiedTweets.count();
        // JavaRDD<Integer> filteredTweetsCount = filteredTweets
        //                                 .map(tweet -> 1)
        //                                 .reduce("+");
                                        //.mapToPair(tweet -> new Tuple2<>(tweet, 1))
                                        //.reduceByKey((a, b) -> Integer.parseInt(a.toString()) + Integer.parseInt(b.toString())); //Converting object to integer type
   
        System.out.println("\n\nNumber of tweets: " + TweetsCount + "\n\n");
        simplifiedTweets.saveAsTextFile(outputFile);

        //final S3Uploader uploader = new S3Uploader(bucket, language); //The prefix is the language
        //uploader.upload(Arrays.asList(filteredTweets));

        long stop = System.currentTimeMillis();
        System.out.println("Done in ms: " + (stop - start));
        
    }
}
