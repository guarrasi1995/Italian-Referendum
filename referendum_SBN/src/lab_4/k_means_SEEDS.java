package lab_4;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;

public class k_means_SEEDS {
	public static void main(String[] args) throws IOException {
		
		//Load scoreYesNo and create map Long,String
		Map<String,long[]> scoreYesNo = new HashMap<String,long[]>();
		Map<Long,String> userid_to_scrennname = new HashMap<Long,String>();
		Scanner scanner = new Scanner(new File("./data/M_score.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			scoreYesNo.put(app[0],new long[] {Long.parseLong(app[1]),Long.parseLong(app[2]),Long.parseLong(app[3])});
			userid_to_scrennname.put(Long.parseLong(app[1]), app[0]);
		}
		scanner.close();
		
		//Load nodeMapper (Integer,Long and Long,Integer)
        Map<Integer,Long> nodeMapper = new HashMap<Integer,Long>();
        Map<Long,Integer> nodeMapperReverse = new HashMap<Long,Integer>();

		scanner = new Scanner(new File("./data/node_mapper.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			nodeMapper.put(Integer.parseInt(app[1]),Long.parseLong(app[0]));
			nodeMapperReverse.put(Long.parseLong(app[0]),Integer.parseInt(app[1]));
		}
		scanner.close();
		
		//Create Map Node_int to vote (NO -1 ... +1 YES)
		Map<Integer,Double> syn = new HashMap<Integer,Double>();
		for(int i : nodeMapper.keySet()) {
			long userid = nodeMapper.get(i);
			String screenname = userid_to_scrennname.get(userid);
			long[] scores = scoreYesNo.get(screenname);
			double vote = (((double)scores[1]-scores[2])/(double)(scores[1]+scores[2]));
			
			/*if(scores[1]>scores[2])
				vote=1;
			else
				vote=-1;*/
			syn.put(i, vote);
			//System.out.println(vote);
		}
	
		//load graph
		WeightedDirectedGraph g = new WeightedDirectedGraph(Collections.max(nodeMapper.keySet()));
		GraphReader.readGraph(g, "./data/sub_graph.gz", true);

        System.out.println("SIZE: " + g.size);

        int[] vert = g.getVertex();
        /*for(int i : vert) {
        	System.out.println(i);
        }*/
        System.out.println("SIZE: " + vert.length);

        
        //SEEDS = K-players
        //Load K-players
        System.out.println();
        System.out.println("K-Players SEEDS");
        System.out.println();
		List<Integer>[] seeds = new ArrayList[2];
		seeds[0] = new ArrayList<Integer>();
		scanner = new Scanner(new File("./data/topAuth/topKYes.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			seeds[0].add(nodeMapperReverse.get(Long.parseLong(app[0])));
		}
		scanner.close();
		
		seeds[1] = new ArrayList<Integer>();
		scanner = new Scanner(new File("./data/topAuth/topKNo.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			seeds[1].add(nodeMapperReverse.get(Long.parseLong(app[0])));
		}
		scanner.close();
		
		//K-means
        Map<Integer,List<Integer>> clusters = Algorithms.k_means_seeds(g, syn, seeds, 2, 20);
        
        //Save clusters
		for(Integer i : clusters.keySet()) {
	        PrintWriter pw = new PrintWriter(new File("./data/clusters/clusterK"+i+".csv"));
	        for(Integer v : clusters.get(i)) {
	        	StringBuilder sb = new StringBuilder();
				sb.append(nodeMapper.get(v)+"\n");
				pw.write(sb.toString());
	        }
	        pw.close();
		}
		
		
		//SEEDS = ALL
        System.out.println();
        System.out.println("ALL SEEDS");
        System.out.println();
		seeds = new ArrayList[2];
		seeds[0] = new ArrayList<Integer>();
		for(int v : vert) {
			if(syn.get(v)==1)
				seeds[0].add(v);
		}

		seeds[1] = new ArrayList<Integer>();
		for(int v : vert) {
			if(syn.get(v)==-1)
				seeds[1].add(v);
		}
		
		//K-means
        clusters = Algorithms.k_means_seeds(g, syn, seeds, 2, 20);
        
        //Save clusters
		for(Integer i : clusters.keySet()) {
	        PrintWriter pw = new PrintWriter(new File("./data/clusters/clusterAll"+i+".csv"));
	        for(Integer v : clusters.get(i)) {
	        	StringBuilder sb = new StringBuilder();
				sb.append(nodeMapper.get(v)+"\n");
				pw.write(sb.toString());
	        }
	        pw.close();
		}
		
        
		//SEEDS = Comb-Auth
        System.out.println();
        System.out.println("Comb-Auth SEEDS");
        System.out.println();
		//Load top Authorities
		seeds = new ArrayList[2];
		seeds[0] = new ArrayList<Integer>();
		scanner = new Scanner(new File("./data/topAuth/topCombYes.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			seeds[0].add(nodeMapperReverse.get(Long.parseLong(app[0])));
		}
		scanner.close();
		
		seeds[1] = new ArrayList<Integer>();
		scanner = new Scanner(new File("./data/topAuth/topCombNo.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			seeds[1].add(nodeMapperReverse.get(Long.parseLong(app[0])));
		}
		scanner.close();
		
		//K-means
        clusters = Algorithms.k_means_seeds(g, syn, seeds, 2, 20);
        
        //Save clusters
		for(Integer i : clusters.keySet()) {
	        PrintWriter pw = new PrintWriter(new File("./data/clusters/clusterComb"+i+".csv"));
	        for(Integer v : clusters.get(i)) {
	        	StringBuilder sb = new StringBuilder();
				sb.append(nodeMapper.get(v)+"\n");
				pw.write(sb.toString());
	        }
	        pw.close();
		}
	}
}
