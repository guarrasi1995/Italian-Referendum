package lab_4;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;

import it.stilo.g.structures.Core;
import it.stilo.g.structures.WeightedUndirectedGraph;
import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.CoreDecomposition;
import it.stilo.g.algo.SubGraphByEdgesWeight;

import twitter4j.TwitterException;

public class Co_occurrence_graph {
	public static void main(String[] args) throws IOException, TwitterException, ParseException, InterruptedException {
		//Load tweets from Lucene
		LoadTweets.load_LUCENE_tweets0("./data/tweets_politici");
		
		//YES
		//Get the clusters files
		File folder = new File("./data/clusters_words/clusters_yes");
		List<String> list_files = new ArrayList<String>();
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.getName()!=".DS_Store")
				list_files.add(fileEntry.getName());
		}
		
		Map<Integer,List<String>> clusters = new HashMap<Integer,List<String>>();
		
		int cont=0;
		//for each cluster load it
		for(String a : list_files) {
			System.out.println("READING "+a);
			clusters.put(cont, new ArrayList<String>());
			
			Scanner scanner = new Scanner(new File("./data/clusters_words/clusters_yes/"+a));
			scanner.useDelimiter(",");
			while(scanner.hasNextLine()) {
				clusters.get(cont).add(scanner.nextLine().split(",")[0]);
			}
			scanner.close();
			cont++;
		}
		
		//for each cluster
		cont=0;
		for(int i : clusters.keySet()) {
			List<String> app = clusters.get(i);
			//initialize the graph
			WeightedUndirectedGraph g = new WeightedUndirectedGraph(app.size());
			//for each couple of words
			for(int j=1; j<app.size(); j++) {
				String term1 = app.get(j);
				System.out.println("Add: "+term1);
				for(int k=0; k<j; k++) {
					String term2 = app.get(k);
					//get how many tweets the words have in common
					double w = LoadTweets.get_co_occurrence_weight(term1, term2, "Y");
					//System.out.print(w);
					if(w>0) {
						//add edge
						g.testAndAdd(j, k, w);
					}
				}
			}
			
			//System.out.println(Arrays.deepToString(g.out));
			
			//System.out.println(Arrays.deepToString(g.in));

			//System.out.println(Arrays.deepToString(g.weights));
			
			//find number of threads available
			int worker = (int) (Runtime.getRuntime().availableProcessors());
			System.out.println(worker);
			//int[] nthresholds = new int[] {5, 50, 100};
			//for(int th : nthresholds) {
			//set weight threshold
			int th = 15;
			System.out.println("THRESHOLD "+th);
			//extract the subgraph that satifies the threshold
			WeightedUndirectedGraph g2 = SubGraphByEdgesWeight.extract(g,th,1);
			
			//list of all the nodes
			int[] all = new int[g2.size];
	        for (int k = 0; k < g2.size; k++) {
	            all[k] = k;
	        }
	        
	        //starting from all the nodes, find all the possible connected components
	        Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g2, all, worker);
			
			//find the biggest connected component
	        Set<Integer> max_comp = new HashSet<Integer>();
	        int max_size = 0;
	        
			for(Set<Integer> c : comps) {
				if(c.size()>max_size) {
					max_comp = c;
					max_size = c.size();
				}
			}
			
			//print the words that are part of the biggest connected component
			System.out.println("CONNECTED COMP:");
			for(int k : max_comp) {
				System.out.println(k + " - " + app.get(k));
			}
			System.out.println();
			
			//from the biggest connected component find the inner most core nodes
			Core c = CoreDecomposition.getInnerMostCore(g, worker);
			
			//save in .csv
			PrintWriter pw = new PrintWriter(new File("./data/cores_words/cores_yes/core"+cont+".csv"));
			System.out.println("CORE:");
			
			//for the core words get the time-series with grain 3h
			int[] list_nodes = c.seq;
			for(int v : list_nodes) {
				StringBuilder sb = new StringBuilder();
				String term = app.get(v);
				sb.append(term);
				
				double[] freq_vector = LoadTweets.get_time_series(term, "Y", 3);
				
				for(double f : freq_vector) {
					sb.append(","+f);
				}
				sb.append("\n");
				pw.write(sb.toString());
				System.out.println(v + " - " + app.get(v));
			}
			System.out.println();
			pw.close();

			//}
			cont++;
		}
		
		
		//NO
		folder = new File("./data/clusters_words/clusters_no");
		list_files = new ArrayList<String>();
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.getName()!=".DS_Store")
				list_files.add(fileEntry.getName());
		}
		
		clusters = new HashMap<Integer,List<String>>();
		
		//for each cluster load it
		cont=0;
		for(String a : list_files) {
			System.out.println("READING "+a);
			clusters.put(cont, new ArrayList<String>());
			
			Scanner scanner = new Scanner(new File("./data/clusters_words/clusters_no/"+a));
			scanner.useDelimiter(",");
			while(scanner.hasNextLine()) {
				clusters.get(cont).add(scanner.nextLine().split(",")[0]);
			}
			scanner.close();
			cont++;
		}
		
		//for each cluster
		cont=0;	
		for(int i : clusters.keySet()) {
			List<String> app = clusters.get(i);
			//initialize the graph
			WeightedUndirectedGraph g = new WeightedUndirectedGraph(app.size());
			//for each couple of words
			for(int j=1; j<app.size(); j++) {
				String term1 = app.get(j);
				System.out.println("Add: "+term1);
				for(int k=0; k<j; k++) {
					String term2 = app.get(k);
					//get how many tweets the words have in common
					double w = LoadTweets.get_co_occurrence_weight(term1, term2, "Y");
					//System.out.print(w);
					if(w>0) {
						//add edge
						g.testAndAdd(j, k, w);
					}
				}
			}
			
			//System.out.println(Arrays.deepToString(g.out));
			
			//System.out.println(Arrays.deepToString(g.in));

			//System.out.println(Arrays.deepToString(g.weights));
			
			//find number of threads available
			int worker = (int) (Runtime.getRuntime().availableProcessors());
			System.out.println(worker);
			//int[] nthresholds = new int[] {5, 50, 100};
			//for(int th : nthresholds) {
			//set weight threshold
			int th = 15;
			System.out.println("THRESHOLD "+th);
			//extract the subgraph that satifies the threshold
			WeightedUndirectedGraph g2 = SubGraphByEdgesWeight.extract(g,th,1);
			
			//list of all the nodes
			int[] all = new int[g2.size];
	        for (int k = 0; k < g2.size; k++) {
	            all[k] = k;
	        }
	        
	        //starting from all the nodes, find all the possible connected components
	        Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g2, all, worker);
			
			//find the biggest connected component
	        Set<Integer> max_comp = new HashSet<Integer>();
	        int max_size = 0;
	        
			for(Set<Integer> c : comps) {
				if(c.size()>max_size) {
					max_comp = c;
					max_size = c.size();
				}
			}
			//print the words that are part of the biggest connected component
			System.out.println("CONNECTED COMP:");
			for(int k : max_comp) {
				System.out.println(k + " - " + app.get(k));
			}
			System.out.println();
			//from the biggest connected component find the k-core nodes
			Core c = CoreDecomposition.getInnerMostCore(g, worker);
			//save in .csv
			PrintWriter pw = new PrintWriter(new File("./data/cores_words/cores_no/core"+cont+".csv"));
			
			System.out.println("CORE:");
			//for the k.core words get the time-series with grain 3h
			int[] list_nodes = c.seq;
			for(int v : list_nodes) {
				StringBuilder sb = new StringBuilder();
				String term = app.get(v);
				sb.append(term);
				
				double[] freq_vector = LoadTweets.get_time_series(term, "N", 3);
				
				for(double f : freq_vector) {
					sb.append(","+f);
				}
				sb.append("\n");
				pw.write(sb.toString());
				System.out.println(v + " - " + app.get(v));
			}
			System.out.println();
			pw.close();

			//}
			cont++;

			//}
		}


	}
}
