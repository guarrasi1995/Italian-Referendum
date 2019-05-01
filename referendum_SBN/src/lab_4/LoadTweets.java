package lab_4;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import twitter4j.TwitterException;

import static java.lang.Math.toIntExact;

public class LoadTweets {
	private static IndexReader indexReaderY;
	private static IndexSearcher searcherY;
	private static IndexReader indexReaderN;
	private static IndexSearcher searcherN;
	private static IndexReader reader;
	private static IndexSearcher searcher;
	private static IndexWriter writer;
	private static IndexWriter writerY;
	private static IndexWriter writerN;

	
	public static void load_tweets0(String folder, List<String> account_politici, List<String[]> data, IndexWriter wry, IndexWriter wrn) throws IOException, TwitterException {
		writerY = wry;
		writerN = wrn;
		
		List<String> list_files = listFilesForFolder(new File(folder), new ArrayList<String>()); //Get all files in folder (each part of the day)
		
		//for each file in the folder
		for(String file : list_files) {
			if(!file.equals(".DS_Store")) {
				InputStream gzip = new GZIPInputStream(new FileInputStream(folder+"/"+file));
				BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		        String line = null;
				//for each line(tweet) in the file
		        while ((line = br.readLine()) != null) {
					//get status (dictionary of the tweet)
		        	StatusWrapper stat_wrap = new StatusWrapper();
		        	stat_wrap.load(line);
		        	
	                //extract screenname
	                String screenname = stat_wrap.getStatus().getUser().getScreenName();
	                int ind = account_politici.indexOf(screenname); //match screenname with list of politicians screennames
	                //we limited the stream to 10 exact days, for later
	                if(ind!=-1 && stat_wrap.getTime()<1481034614348L) //time<... to get exact 10 days
	                {
						//save the tweet in Lucene Index
	                	save_tweet0(stat_wrap, data.get(ind)[3]);
	                	System.out.println("						" + screenname);
	                }
		        }
		        br.close();
			}
		}
		
	}
	
	public static void load_tweets1(String folder, List<String> account_politici, List<String[]> data, List<String> yesWords, List<String> noWords, Map<String,long[]> scoreYesNo, IndexWriter wr) throws IOException, TwitterException {
		
		writer = wr;
		
		List<String> list_files = listFilesForFolder(new File(folder), new ArrayList<String>());
		
		//for each file in the folder
		for(String file : list_files) {
			if(!file.equals(".DS_Store")) {
				InputStream gzip = new GZIPInputStream(new FileInputStream(folder+"/"+file));
				BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		        String line = null;
				//for each line in the file (tweet)
		        while ((line = br.readLine()) != null) {
		        	StatusWrapper stat_wrap = new StatusWrapper();
		        	stat_wrap.load(line);
	                //extract screenname
	                String screenname = stat_wrap.getStatus().getUser().getScreenName();
	                int ind = account_politici.indexOf(screenname);
	                //we limited the stream to 10 exact days, for later
	                if(ind!=-1 && stat_wrap.getTime()<1481034614348L) //politician
	                {
	                	//get vote of the politician
	                	String vote = data.get(ind)[3];
	                	int vyes = vote.equals("Y") ? 25 : 0;
	                	//add vote to map scoreYesNo
	                	if(!scoreYesNo.containsKey(screenname))
	                		scoreYesNo.put(screenname, new long[] {stat_wrap.getStatus().getUser().getId(), vyes,25-vyes});
	                	else
	                		scoreYesNo.put(screenname, new long[] {stat_wrap.getStatus().getUser().getId(), scoreYesNo.get(screenname)[1]+vyes, scoreYesNo.get(screenname)[2]+25-vyes});
						//save the tweet in Lucene Index
	                	save_tweet1(stat_wrap, vote, scoreYesNo.get(screenname));
	                	System.out.println("						" + screenname);
	                } else if (stat_wrap.getTime()<1481034614348L){
	                	//compute score based on hashtags
	                	int[] score = new int[] {0,0};
	                	String text = stat_wrap.getStatus().getText();
	                	/*
		                for(String s : yesTerms) {
		                	if(text.contains(s))
		                		score[0]++;
		                }*/
		                
		                for(String s : yesWords) {
		                	if(text.contains(s))
		                		score[0]+=2;
		                }
		                /*
		                for(String s : noTerms) {
		                	if(text.contains(s))
		                		score[1]++;
		                }*/
		                
		                for(String s : noWords) {
		                	if(text.contains(s))
		                		score[1]+=2;
		                }
		                
	                	//compute score based on politicians referenced
		                for(String s : account_politici) {
		                	if(text.contains(s))
		                		score[data.get(account_politici.indexOf(s))[3].equals("Y") ? 0 : 1]+=3;
		                }
		                
		                //If sum of scores > 6 (so to limit number of tweets)
		                if(score[0]+score[1]>6) {
		                	if(!scoreYesNo.containsKey(screenname))
		                		scoreYesNo.put(screenname, new long[] {stat_wrap.getStatus().getUser().getId(), score[0], score[1]});
		                	else
		                		scoreYesNo.put(screenname, new long[] {scoreYesNo.get(screenname)[0], scoreYesNo.get(screenname)[1]+score[0], scoreYesNo.get(screenname)[2]+score[1]});
		                	String vote = score[1]>score[0] ? "N" : "Y";
							//save the tweet in Lucene Index
			                save_tweet1(stat_wrap, vote, scoreYesNo.get(screenname));
		                	System.out.println("						" + screenname + "; "+ (score[0]+score[1]));
		                }
	                }
		        }
		        br.close();
			}
		}
		
	}
	
	public static void load_LUCENE_tweets0(String folder) throws IOException, TwitterException {
		//Load tweets from the (Y/N) Lucene Indexes
		Directory oldDirectoryY = new SimpleFSDirectory(new File(folder+"_Y"));
	    Directory rdirY = new RAMDirectory(oldDirectoryY, IOContext.DEFAULT);
	    indexReaderY = DirectoryReader.open(rdirY); 
	    searcherY = new IndexSearcher(indexReaderY);
	    
	    Directory oldDirectoryN = new SimpleFSDirectory(new File(folder+"_N"));
	    Directory rdirN = new RAMDirectory(oldDirectoryN, IOContext.DEFAULT);
	    indexReaderN = DirectoryReader.open(rdirN); 
	    searcherN = new IndexSearcher(indexReaderN);
	}
	
	public static void load_LUCENE_tweets1(String folder) throws IOException, TwitterException {
		//Load tweets from the Lucene Index
		Directory oldDirectory = new SimpleFSDirectory(new File(folder));
	    Directory rdir = new RAMDirectory(oldDirectory, IOContext.DEFAULT);
	    reader = DirectoryReader.open(rdir); 
	    searcher = new IndexSearcher(reader);
	}
	
	public static List<String> listFilesForFolder(File folder, List<String> app) {
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.isDirectory())
				app.addAll(listFilesForFolder(fileEntry, app));
			else
				app.add(fileEntry.getName());
		}
		return app;
	}
	
	public static void save_tweet0(StatusWrapper tweet, String vote) throws IOException {		
		//for each tweet create a doc with fields: screenname, id, createdAt, text, vote
		Document doc = new Document();
		
		doc.add(new StringField("screenname", tweet.getStatus().getUser().getScreenName(), Field.Store.YES));
		
		doc.add(new LongField("id", tweet.getStatus().getId(), Field.Store.YES)); //id of tweet
		
		doc.add(new LongField("createdAt", tweet.getTime(), Field.Store.YES));
		
		doc.add(new TextField("text", tweet.getStatus().getText(), Field.Store.YES)); //text field is parsed by analyzer
		
		doc.add(new StringField("vote", vote, Field.Store.YES));
		//divide the tweets in Lucene Index Y/N
		if(vote.equals("Y")) {
			writerY.addDocument(doc);
				
			writerY.commit();
		} else {
			writerN.addDocument(doc);
				
			writerN.commit();
		}
		
	}
	
	public static void save_tweet1(StatusWrapper tweet, String vote, long[] score) throws IOException {		
		//for each tweet create a doc with fields: screenname, userid, id, createdAt, text, vote, scoreYes, scoreNo
		Document doc = new Document();
		
		doc.add(new StringField("screenname", tweet.getStatus().getUser().getScreenName(), Field.Store.YES));
		
		doc.add(new LongField("userid", tweet.getStatus().getUser().getId(), Field.Store.YES));
		
		doc.add(new LongField("id", tweet.getStatus().getId(), Field.Store.YES));
		
		doc.add(new LongField("createdAt", tweet.getTime(), Field.Store.YES));
		
		doc.add(new TextField("text", tweet.getStatus().getText(), Field.Store.YES));
		
		doc.add(new StringField("vote", vote, Field.Store.YES));
		
		doc.add(new LongField("scoreYes", score[0], Field.Store.YES)); //of the tweet

		doc.add(new LongField("scoreNo", score[1], Field.Store.YES)); //of the tweet

		writer.addDocument(doc);
				
		writer.commit();
	}
		
	public static int[] get_num_users0() throws IOException {
		//create a set of unique screenname in each Lucene Index (Y/N)
		Set<String> uniqueUsersY = new HashSet<String>();
		Set<String> uniqueUsersN = new HashSet<String>();

		
		//for each tweet, get screenname (Y)
		for(int i=0; i<indexReaderY.maxDoc(); i++) {
			uniqueUsersY.add(indexReaderY.document(i).get("screenname"));
			//System.out.println(indexReaderY.document(i).get("screenname"));
		}
		
		//for each tweet, get screenname (N)
		for(int i=0; i<indexReaderN.maxDoc(); i++) {
			uniqueUsersN.add(indexReaderN.document(i).get("screenname"));
			//System.out.println(indexReaderN.document(i).get("screenname"));
		}
		
		//return the size of the sets
		return(new int[] {uniqueUsersY.size(),uniqueUsersN.size()});
	}
	
	public static int get_num_users1() throws IOException {
		//create a set of unique screenname in Lucene Index
		Set<String> uniqueUsers = new HashSet<String>();
		
		for(int i=0; i<reader.maxDoc(); i++) {
			uniqueUsers.add(reader.document(i).get("screenname"));
			//System.out.println(indexReaderY.document(i).get("screenname"));
		}
		//return the size of the set
		return(uniqueUsers.size());
	}
	
	public static int[] get_num_tweets0() throws IOException {
		//get the id of the last docs
		return(new int[] {indexReaderY.maxDoc(),indexReaderN.maxDoc()});
	}
	
	public static int get_num_tweets1() throws IOException {
		//get the id of the last doc
		return(reader.maxDoc());
	}
	
	public static void save_created_at() throws IOException {
		long[] createdAt = new long[indexReaderY.maxDoc()];
		//for each saved tweet Y get the info CreatedAt
		for(int i=0; i<indexReaderY.maxDoc(); i++) {
			createdAt[i] = Long.parseLong(indexReaderY.document(i).get("createdAt"));
		}
		//save in distribution_Y.csv (one line per time)
		PrintWriter pw = new PrintWriter(new File("./data/distribution_Y.csv"));
        
        for(long i : createdAt) {
        	StringBuilder sb = new StringBuilder();
        	
			sb.append(i + "\n");
			
			pw.write(sb.toString());
		}
        
        pw.close();
        
        createdAt = new long[indexReaderN.maxDoc()];
		//for each saved tweet N get the info CreatedAt
		for(int i=0; i<indexReaderN.maxDoc(); i++) {
			createdAt[i] = Long.parseLong(indexReaderN.document(i).get("createdAt"));
		}
		
		//save in distribution_N.csv (one line per time)
		pw = new PrintWriter(new File("./data/distribution_N.csv"));
        for(long i : createdAt) {
        	StringBuilder sb = new StringBuilder();
        	
			sb.append(i + "\n");
			
			pw.write(sb.toString());
		}
        
        pw.close();
	}
	
	
	public static void top_terms_to_sax(int alph_size) throws Exception {
		//CreatedAt first tweet
		//long min_epoch = 1480170614348L;
		//long max_epoch = 1481034614348L;
		
		
		//choose parameters
		int alphabetSize = alph_size;
		double nThreshold = 0.0001;
		// instantiate sax classes
		NormalAlphabet na = new NormalAlphabet();
		SAXProcessor sp = new SAXProcessor();
		
		
		String[][] saxMapY = new String[1000][2];
		String[][] saxMapN = new String[1000][2];
		
		//YES
		int cont=0;
		//get 1000 most frequent terms
		TermStats[] commonTerms = HighFreqTerms.getHighFreqTerms(indexReaderY, 1004, "text");
		//words to remove
		List<String> stop_words = new ArrayList<String>(Arrays.asList("https","rt","t.co","t.c"));
		//for each word
		for (TermStats commonTerm : commonTerms) {
			String term = commonTerm.termtext.utf8ToString(); //transform TermStats to String
			//System.out.println(term);
			
			//check if the term is not one of those to remove
			if(!stop_words.contains(term)) {
				//transform it in time-series with given grain (12h)
				double[] freq_vector = get_time_series(term, "Y", 12);
				
				//SAX
				// perform the discretization
				SAXRecords res = sp.ts2saxByChunking(freq_vector, freq_vector.length, na.getCuts(alphabetSize), nThreshold);
				// print the output
				String sax = res.getSAXString("");
				System.out.println(sax);
				//save in map (term: SaxString)
				saxMapY[cont][0] = term;
				saxMapY[cont][1] = sax;
				cont++;
			}
		}
		
		//save map in csv (term, SaxString)
		PrintWriter pw = new PrintWriter(new File("./data/saxY.csv"));
        for(String[] i : saxMapY) {
        	StringBuilder sb = new StringBuilder();
			for(int j=0; j<i.length; j++) {
				if(j==0)
					sb.append(i[j]);
				else
					sb.append("," + i[j]);
			}
			sb.append("\n");
			pw.write(sb.toString());
		}
        pw.close();
	    System.out.println("String[] data is saved in saxY.csv");
	  
		
		cont=0;
		//List<String> topTermsN = new ArrayList<String>();
		commonTerms = HighFreqTerms.getHighFreqTerms(indexReaderN, 1004, "text");
		stop_words = new ArrayList<String>(Arrays.asList("https","rt","t.co","t.c"));
		
		//for each word
		for (TermStats commonTerm : commonTerms) {
			String term = commonTerm.termtext.utf8ToString();
			//topTermsN.add(term);
			//System.out.println(commonTerm.termtext.utf8ToString());
			//check if the term is not one of those to remove
			if(!stop_words.contains(term)) {
				double[] freq_vector = get_time_series(term, "N", 12);
				
				//SAX
				// perform the discretization
				SAXRecords res = sp.ts2saxByChunking(freq_vector, freq_vector.length, na.getCuts(alphabetSize), nThreshold);
				// print the output
				String sax = res.getSAXString("");
				System.out.println(sax);
				//save in map (term: SaxString)
				saxMapN[cont][0] = term;
				saxMapN[cont][1] = sax;
				cont++;
			}
		}
		
		//save map in csv (term, SaxString)
		pw = new PrintWriter(new File("./data/saxN.csv"));
        for(String[] i : saxMapN) {
        	StringBuilder sb = new StringBuilder();
        	for(int j=0; j<i.length; j++) {
				if(j==0)
					sb.append(i[j]);
				else
					sb.append("," + i[j]);
			}			
        	sb.append("\n");
			pw.write(sb.toString());
		}
        pw.close();
	    System.out.println("String[] data is saved in saxN.csv");
	}
	
	public static double[] get_time_series(String term, String vote, int grain) throws IOException {
		IndexSearcher searcher;
		IndexReader reader;
		if(vote=="Y") {
			searcher = searcherY;
			reader = indexReaderY;
		}
		else {
			searcher = searcherN;
			reader = indexReaderN;
		}
		
		//we will have hours*day/grain values time-series
		double[] freq_vector = new double[10*24/grain];
		//CreatedAt first Tweet
		long min_epoch = 1480170614348L;
		//topTermsY.add(term);
		
		//query the term and get tweets that use that term
		Query q = new TermQuery(new Term("text",term));
		TopDocs top = searcher.search(q, reader.maxDoc());
		ScoreDoc[] hits = top.scoreDocs;
		
		//for each returned tweet get the CreatedAt and find in which bin it falls in
		for(ScoreDoc entry : hits) {
			Document doc = searcher.doc(entry.doc);				
			int ind = toIntExact((Long.parseLong(doc.get("createdAt"))-min_epoch)/(3600000L*grain));
			freq_vector[ind]++;
		}
		
		
		//Normalization
		double mx = Collections.max(Arrays.asList(ArrayUtils.toObject(freq_vector)));
		double mn = Collections.min(Arrays.asList(ArrayUtils.toObject(freq_vector)));
		
		for(int i=0; i<freq_vector.length; i++) {
			System.out.print(freq_vector[i]+"/");
			freq_vector[i]=(freq_vector[i]-mn)/(mx-mn);
		}
		System.out.println();

		return(freq_vector);
	}
	
	public static double get_co_occurrence_weight(String term1, String term2, String vote) throws ParseException, IOException {
		//query the Lucene Index  with AND query between the two words given in input
		Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_41);
		QueryParser parser = new QueryParser(Version.LUCENE_41, "text", analyzer);
		Query q = parser.parse(term1+" AND "+term2);
		
		IndexSearcher searcher;
		IndexReader reader;
		//choose the correct index
		if(vote=="Y") {
			searcher = searcherY;
			reader = indexReaderY;
		}
		else {
			searcher = searcherN;
			reader = indexReaderN;
		}
		
		TopDocs top = searcher.search(q, reader.maxDoc());
		ScoreDoc[] hits = top.scoreDocs;
		//how many tweets are in common betweeen the 2 words?
		return(hits.length);
	}
}
