package lab_4;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import twitter4j.TwitterException;

public class FilterTweets1 {
	public static void main(String[] args) throws IOException, TwitterException {
		//from plotici2.csv divide the info in 2 lists: all info on politician, twitter account
		List<String[]> politici = new ArrayList<String[]>();
		List<String> account_politici = new ArrayList<String>();
		Scanner scanner = new Scanner(new File("./data/politici2.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(";");
			politici.add(app);
			account_politici.add(app[2]);
		}
		scanner.close();
		
		
		//Load YES and NO terms from cores
		/*
		//Load part 0.3 terms YES
		File folder = new File("./data/cores_words/cores_yes");
		List<String> list_files = new ArrayList<String>();
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.getName()!=".DS_Store")
				list_files.add(fileEntry.getName());
		}
		
		List<String> yesTerms = new ArrayList<String>();
		
		for(String a : list_files) {
			scanner = new Scanner(new File("./data/cores_words/cores_yes/"+a));
			scanner.useDelimiter(",");
			while(scanner.hasNextLine()) {
				yesTerms.add(scanner.nextLine().split(",")[0]);
			}
			scanner.close();
		}
		
		//Load part 0.3 terms NO
		folder = new File("./data/cores_words/cores_no");
		list_files = new ArrayList<String>();
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.getName()!=".DS_Store")
				list_files.add(fileEntry.getName());
		}
		
		List<String> noTerms = new ArrayList<String>();
		
		for(String a : list_files) {
			scanner = new Scanner(new File("./data/cores_words/cores_no/"+a));
			scanner.useDelimiter(",");
			while(scanner.hasNextLine()) {
				noTerms.add(scanner.nextLine().split(",")[0]);
			}
			scanner.close();
		}*/
		
		
		//Create lists of yes and no words
        List<String> yesWords = new ArrayList<String>(Arrays.asList("#iovotosi","#iovotosì","#iodicosi","#iodicosì","#iohovotatosi","#iohovotatosì","#votasi","#votasì","#votosi","#votosì","#bastaunsi","#bastaunsì","#bufaledelno","#bufaladelno","#si","#sì"));
        List<String> noWords = new ArrayList<String>(Arrays.asList("#iovotono","#iodicono","#iohovotatono","#votano","#votono","#bastaunno","#bufaledelsi","#bufaledelsì","#no","#noivotiamono","#ragionidelno","#unitixilno","#votiamono"));

        //Create Map with <screenname, {userid, scoreYES, scoreNO}>
        Map<String,long[]> scoreYesNo = new HashMap<String,long[]>();
        
		//all of the folder names (folder = day)
		String[] folders_list = new String[] {"day-1480170614348","day-1480257098290","day-1480343526103","day-1480430203069","day-1480516673438","day-1480603295223","day-1480690014802","day-1480776565169","day-1480863128958","day-1480949681548"}; 
		//String[] folders_list = new String[] {"day-1480170614348"};

		//extract the tweets form the stream and create lucene index
		File f = new File("./data/tweets_correlati");
		Directory dir = new SimpleFSDirectory(f);
		Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
		IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_41, analyzer);
		IndexWriter writer = new IndexWriter(dir,cfg);

		//extract the tweets of the politicins for each day
		for(String fold : folders_list) {
			System.out.println("FOLDER "+fold);
			LoadTweets.load_tweets1("./data/stream/"+fold, account_politici, politici, yesWords, noWords, scoreYesNo, writer);			
		}
		writer.close();
		
		//save scoreYesNo to file
		PrintWriter pw = new PrintWriter(new File("./data/M_score.csv"));
		for(String screenname : scoreYesNo.keySet()) {
			long[] app = scoreYesNo.get(screenname);
			StringBuilder sb = new StringBuilder();
			sb.append(screenname+","+app[0]+","+app[1]+","+app[2]+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		System.out.println("DONE");
	}
}
