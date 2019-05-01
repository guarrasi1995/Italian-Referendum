package lab_4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import twitter4j.TwitterException;

public class FilterTweets0 {
	public static void main(String[] args) throws IOException, TwitterException {
		//from plotici2.csv divide the info in 2 lists: all info on politician, twitter account
		List<String[]> politici = new ArrayList<String[]>();
		List<String> account_politici = new ArrayList<String>();
		
		Scanner scanner = new Scanner(new File("./data/politici2.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(";"); //excel saves csv with delimiter ;
			politici.add(app);
			account_politici.add(app[2]);
		}
		scanner.close();
		
		
		
		//all of the folder names (folder = day)
		String[] folders_list = new String[] {"day-1480170614348","day-1480257098290","day-1480343526103","day-1480430203069","day-1480516673438","day-1480603295223","day-1480690014802","day-1480776565169","day-1480863128958","day-1480949681548"}; 
		//String[] folders_list = new String[] {"day-1480170614348"}; 

		//Initialize 2 lucene indexes (Y/N)
		File fY = new File("./data/tweets_politici_Y");
		Directory dirY = new SimpleFSDirectory(fY);
		Analyzer analyzerY = new ItalianAnalyzer(Version.LUCENE_41);
		IndexWriterConfig cfgY = new IndexWriterConfig(Version.LUCENE_41, analyzerY);
		IndexWriter writerY = new IndexWriter(dirY,cfgY);

		File fN = new File("./data/tweets_politici_N");
		Directory dirN = new SimpleFSDirectory(fN);
		Analyzer analyzerN = new ItalianAnalyzer(Version.LUCENE_41);
		IndexWriterConfig cfgN = new IndexWriterConfig(Version.LUCENE_41, analyzerN);
		IndexWriter writerN = new IndexWriter(dirN,cfgN);
		
		//extract the tweets of the politicians for each day
		for(String folder : folders_list) {
			System.out.println("FOLDER "+folder);
			LoadTweets.load_tweets0("./data/stream/"+folder, account_politici, politici, writerY, writerN);			
		}
		
		writerY.close();
		writerN.close();
		
		System.out.println("DONE");
	}
}
