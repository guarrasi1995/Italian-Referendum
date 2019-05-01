package lab_4;

import net.seninp.jmotif.sax.SAXException;
import java.io.IOException;

import twitter4j.TwitterException;

public class stat_1 {
	public static void main(String[] args) throws IOException, TwitterException, SAXException {
		
		LoadTweets.load_LUCENE_tweets1("./data/tweets_correlati");
		
		int app =  LoadTweets.get_num_users1();
		System.out.println("Num Users: "+ app);

		int app2 = LoadTweets.get_num_tweets1();
		System.out.println("Num Tweets: "+ app2);
	}
}
