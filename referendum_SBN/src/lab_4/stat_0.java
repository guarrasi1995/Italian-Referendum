package lab_4;

import java.io.IOException;

import twitter4j.TwitterException;

public class stat_0 {
	public static void main(String[] args) throws IOException, TwitterException {
		//load tweets form lucene index
		LoadTweets.load_LUCENE_tweets0("./data/tweets_politici");
		
		//How many users(Y/N)?
		int[] app =  LoadTweets.get_num_users0();
		System.out.println("Num Users YES: "+ app[0]);
		System.out.println("Num Users NO: "+ app[1]);
		
		//How many Tweets(Y/N)?
		int[] app2 = LoadTweets.get_num_tweets0();
		System.out.println("Num Tweets YES: "+ app2[0]);
		System.out.println("Num Tweets NO: "+ app2[1]);
		
		//save in csv files all the createdAt info for each tweet (Y/N)
		LoadTweets.save_created_at();
		
        System.out.println("DONE");
	}
}
