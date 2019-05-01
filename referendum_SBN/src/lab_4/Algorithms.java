package lab_4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import it.stilo.g.structures.WeightedDirectedGraph;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class Algorithms {
	
	public static String[] centers;
	public static int[] centers_seeds;
	public static double[][] distances;
	public static int num_clusters;
	public static int[] assignments;
	public static String[][] sax;
	public static Map<String,Double> sax_dist_table;
	public static WeightedDirectedGraph graph;
	public static int[][] adjacency_matrix;
	public static Map<Integer,Double> scoreYesNo;
	public static int[] vertices;
	public static Map<Integer,Integer> vert_to_ind;
	
	public static Map<Integer,List<String>> k_means_sax(String[][] sx, int k, int mMaxIterations, int alph_size) throws SAXException {
		sax=sx;
		num_clusters = k;
		sax_dist_table = new HashMap<String, Double>();
		
		//Create sax distance table
		NormalAlphabet na = new NormalAlphabet();
		double[] cuts = na.getCuts(15);
		
		//get possible characters based on alphabet size
		String poss_chars = "abcdefghijklmnopqrstuvwxyz".substring(0, alph_size);
		//create distance table for all characters
		for(int i=0; i<poss_chars.length(); i++) {
			char char1 = poss_chars.charAt(i); 
			sax_dist_table.put(char1+""+char1, 0.);
			for(int j=i+1; j<poss_chars.length(); j++) {
				char char2 = poss_chars.charAt(j);
				if(j==i+1) {
					sax_dist_table.put(char1+""+char2, 0.);
					sax_dist_table.put(char2+""+char1, 0.);
				}
				else {
					sax_dist_table.put(char1+""+char2, cuts[Math.max(i, j)-1]-cuts[Math.min(i, j)]);
					sax_dist_table.put(char2+""+char1, cuts[Math.max(i, j)-1]-cuts[Math.min(i, j)]);
				}
			}
		}
		
		//Print sax table
		/*
		for(String s : sax_dist_table.keySet()) {
			System.out.println(s + ": "+sax_dist_table.get(s));
		}*/
		
		
	    System.out.println("K-Means clustering started");
	    
	    // Randomly initialize the cluster centers creating the array
	    initCenters(alph_size);
	            
	    System.out.println("... centers initialized");

	    // Perform the initial computation of distances.
	    computeDistances();

	    // Make the initial cluster assignments.
	    assignments = new int[sax.length];
	    makeAssignments();
	    
	    // Number of moves in the iteration and the iteration counter.
	    int moves = 0, it = 0;
	    
	    // Main Loop:
	    //
	    // Two stopping criteria:
	    // - no moves in makeAssignments 
	    //   (moves == 0)
	    // OR
	    // - the maximum number of iterations has been reached
	    //   (it == mMaxIterations)
	    //
	    do {

	    	// Compute the centers of the clusters that need updating.
	    	computeCenters();
	      
	    	// Compute the stored distances between the updated clusters and the coordinates.
	    	computeDistances();
	
	    	// Make this iteration's assignments.
	    	moves = makeAssignments();
	
	    	it++;
	      
	    	System.out.println("... iteration " + it + " moves = " + moves);

	    } while (moves > 0 && it < mMaxIterations);

	    return(generateFinalClusters());	            
	}
	
	public static Map<Integer,List<Integer>> k_means_seeds(WeightedDirectedGraph g, Map<Integer,Double> syn, List<Integer>[] seeds, int k, int mMaxIterations) {
		graph = g;
		num_clusters = k;
		vertices = g.getVertex();
		adjacency_matrix = new int[vertices.length][vertices.length];
		distances = new double[vertices.length][num_clusters];
		
		//create map vertex to matrix row/column
		vert_to_ind = new HashMap<Integer,Integer>();
		int cont=0;
		for(int v : vertices) {
			vert_to_ind.put(v, cont);
			cont++;
		}
		
		//create adjacency matrix
		int[][] app = graph.out;
		int cont1 = 0;
		for(int v1 : vertices) {
			int cont2=0;
			for(int v2 : vertices) {
				if(ArrayUtils.contains(app[v1], v2)) {
					adjacency_matrix[cont1][cont2] = 1;
				}
				cont2++;
			}
			cont1++;
		}
		
		app = graph.in;
		cont1 = 0;
		for(int v1 : vertices) {
			int cont2=0;
			for(int v2 : vertices) {
				if(ArrayUtils.contains(app[v1], v2)) {
					adjacency_matrix[cont1][cont2] = 1;
				}
				cont2++;
			}
			cont1++;
		}
		
		scoreYesNo = syn;
		
		// Note the start time.
	    //long startTime = System.currentTimeMillis();
	    
	    System.out.println("K-Means clustering started");
	    
	    // Randomly initialize the cluster centers creating the array
	    initCenters(seeds);
	            
	    System.out.println("... centers initialized");

	    // Perform the initial computation of distances.
	    computeDistancesSeeds();

	    // Make the initial cluster assignments.
	    assignments = new int[graph.size];
	    makeAssignmentsSeeds();
	    
	    // Number of moves in the iteration and the iteration counter.
	    int moves = 0, it = 0;
	    
	    // Main Loop:
	    //
	    // Two stopping criteria:
	    // - no moves in makeAssignments 
	    //   (moves == 0)
	    // OR
	    // - the maximum number of iterations has been reached
	    //   (it == mMaxIterations)
	    //
	    do {

	    	// Compute the centers of the clusters that need updating.
	    	computeCentersSeeds();
	      
	    	// Compute the stored distances between the updated clusters and the
	    	// coordinates.
	    	computeDistancesSeeds();
	
	    	// Make this iteration's assignments.
	    	moves = makeAssignmentsSeeds();
	
	    	it++;
	      
	    	System.out.println("... iteration " + it + " moves = " + moves);

	    } while (moves > 0 && it < mMaxIterations);

	    //Return clusters
	    return(generateFinalClustersSeeds());
	    
	    
	}
	
	
	public static double string_dist(String a, String b) {
		//compute distance between 2 strings, char by char
		double dist = 0;
		for(int i=0; i<a.length(); i++) {
			Character c1 = a.charAt(i);
			Character c2 = b.charAt(i);
			//dist += (c1 - c2)^2;
			dist += sax_dist_table.get(c1+""+c2);
		}
		
		return(dist);
	}
	
	public static double dist(double[] vectorA, int[] vectorB) {
		//compute cosine similarity
		double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   

	    double dist = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	    /*if(dist==Double.NaN) 
	    	return 0;
	    else*/
    	return dist;
	}
	
	public static double dist(int[] vectorA, int[] vectorB) {
		//compute cosine similarity
		double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += (double) vectorA[i] * (double) vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    
	    double dist = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	    /*if(dist==Double.NaN) 
	    	return 0;
	    else*/
    	return dist;
	}
	
	public static void initCenters(int as) {
		//initialize the centroids randomly
		centers = new String[num_clusters];
		
		Set<String> app = new HashSet<String>();
		
		while(app.size()<num_clusters) {
			String poss_chars = "abcdefghijklmnopqrstuvwxyz".substring(0, as);
	        StringBuilder sb = new StringBuilder();
	        Random rnd = new Random();
	        while (sb.length() < 20) {
	            int index = (int) (rnd.nextFloat() * poss_chars.length());
	            sb.append(poss_chars.charAt(index));
	        }
	        app.add(sb.toString());
		}
		
		int cont=0;
		for(String i : app) {
			centers[cont] = i;
			System.out.println("START CENTER: "+i);
			cont++;
		}
	}
	
	public static void initCenters(List<Integer>[] seeds) {
		//initialize the centroids in SEEDS
		centers_seeds = new int[num_clusters];
		
		double[][] int_centers = new double[num_clusters][vertices.length];
		double[] vote_centers = new double[num_clusters];
		//Find point mean of the SEEDS
		for(int k=0; k<num_clusters; k++) {
			for(int v : seeds[k]) {
				for(int j=0; j<vertices.length; j++)
				{	
					int_centers[k][j] += ((double)adjacency_matrix[vert_to_ind.get(v)][j])/seeds[k].size();
				}
				vote_centers[k]+=((double)scoreYesNo.get(v))/seeds[k].size();
			}
			//System.out.println(vote_centers[k]);
		}
		
		//get node nearest to point found
		for(int k=0; k<num_clusters; k++) {
			double min_dist = 2;
			int medoid = 0;
			for(int v : seeds[k]) {
				double adj_dist = dist(int_centers[k], adjacency_matrix[vert_to_ind.get(v)]);
				double vote_dist = Math.abs(vote_centers[k]-scoreYesNo.get(v))/2;
				
				double new_dist = 0.5*adj_dist+0.5*vote_dist; //new dist is a mean of the adjacency distance and score distance
				//System.out.println(adj_dist +"+"+vote_dist+"="+new_dist);
				
				distances[vert_to_ind.get(v)][k] = new_dist;
				
				if(new_dist<min_dist) {
					min_dist=new_dist;
					medoid = v;
				}
			}
			centers_seeds[k] = medoid;
		}
		
		for(int i : centers_seeds) {
    		System.out.println("INIT CENTER: "+i);
    	}
	}
	
	
	public static void computeDistances() {
		//compute distances between every sax string and centroids
	    distances = new double[sax.length][num_clusters];
	    
	    for(int i=0; i<sax.length; i++) {
	    	for(int j=0; j<num_clusters; j++) {
	    		distances[i][j] = string_dist(centers[j],sax[i][1]);
	    	}
	    }
	}
	
	public static void computeDistancesSeeds() {
		//compute distances between every node and centroids
	    distances = new double[vertices.length][num_clusters];
	    
	    for(int v : vertices) {
	    	for(int k=0; k<num_clusters; k++) {
	    		//System.out.println(centers_seeds[k]);
	    		double adj_dist = dist(adjacency_matrix[vert_to_ind.get(centers_seeds[k])], adjacency_matrix[vert_to_ind.get(v)]);
				double vote_dist = Math.abs(scoreYesNo.get(centers_seeds[k])-scoreYesNo.get(v));
				
				double new_dist = 0.5*adj_dist+0.5*vote_dist; //new dist is a mean of the adjacency distance and score distance
				
	    		distances[vert_to_ind.get(v)][k] = new_dist;
	    	}
	    }
	}
	
	public static int makeAssignments() {
		//decide to which cluster a term most be part of (the closest)
		int num_moves = 0;
		
		for(int i=0; i<sax.length; i++) {
			double min_dist = distances[i][0];
			int min_index = 0;
	    	for(int j=1; j<num_clusters; j++) {
	    		double app = distances[i][j];
	    		if(app<min_dist) {
	    			min_dist = app;
	    			min_index = j;
	    		}
	    	}
	    	
	    	if (assignments[i] != min_index) {
	    		assignments[i] = min_index;
	    		num_moves++;
	    	}
	    }
		
		return(num_moves);
	}
	
	public static int makeAssignmentsSeeds() {
		//decide to which cluster a node most be part of (the closest)
		int num_moves = 0;
		
		for(int v : vertices) {
			double min_dist = distances[vert_to_ind.get(v)][0];
			int min_index = 0;
	    	for(int j=1; j<num_clusters; j++) {
	    		double app = distances[vert_to_ind.get(v)][j];
	    		if(app<min_dist) {
	    			min_dist = app;
	    			min_index = j;
	    		}
	    	}
	    	
	    	if (assignments[vert_to_ind.get(v)] != min_index) {
	    		assignments[vert_to_ind.get(v)] = min_index;
	    		num_moves++;
	    	}
	    }
		
		return(num_moves);
	}

	public static void computeCenters() {
		//compute the new centroids (the mean string, char by char the mean)
		int[][] int_centers = new int[num_clusters][sax[0][1].length()];
		int[] points_per_cluster = new int[num_clusters];
		
		for(int i=0; i<sax.length; i++) {
			String str = sax[i][1];
			for(int j=0; j<str.length(); j++)
			{
				int_centers[assignments[i]][j] += str.charAt(j); 
			}
			points_per_cluster[assignments[i]]++;
	    }
		
		for(int j=0; j<num_clusters; j++) {
			if(points_per_cluster[j]!=0)
				centers[j] = int_to_string(int_centers[j], points_per_cluster[j]);
		}
		
    	for(String i : centers) {
    		System.out.println("NEW CENTER: "+i);
    	}
	}
	
	
	public static void computeCentersSeeds() {	
		//compute the new centroids
		centers_seeds = new int[num_clusters];
		
		double[][] int_centers = new double[num_clusters][vertices.length];
		double[] vote_centers = new double[num_clusters];
		int[] points_per_cluster = new int[num_clusters];
		
		//Find mean of nodes
		for(int v : vertices) {
			for(int j=0; j<vertices.length; j++)
			{
				int_centers[assignments[vert_to_ind.get(v)]][j] += adjacency_matrix[vert_to_ind.get(v)][j];
			}
			vote_centers[assignments[vert_to_ind.get(v)]]+=scoreYesNo.get(v);
			points_per_cluster[assignments[vert_to_ind.get(v)]]++;
		}
		
		for(int k=0; k<num_clusters; k++) {
			for(int j=0; j<vertices.length; j++)
			{
				int_centers[k][j] /= points_per_cluster[k];
			}
			
			vote_centers[k] /= points_per_cluster[k];
		}
		
		//find nearest node
		for(int k=0; k<num_clusters; k++) {
			double min_dist = 2;
			int medoid = 0;
			for(int v : vertices) {
				double adj_dist = dist(int_centers[k], adjacency_matrix[vert_to_ind.get(v)]);
				double vote_dist = Math.abs(vote_centers[k]-scoreYesNo.get(v))/2;
				
				double new_dist = 0.5*adj_dist+0.5*vote_dist;
				
				//System.out.println(adj_dist +"+"+vote_dist+"="+new_dist);
				
				distances[vert_to_ind.get(v)][k] = new_dist;
				
				
				if(new_dist<min_dist) {
					min_dist=new_dist;
					medoid = v;
				}
			}
			centers_seeds[k] = medoid;
		}
		
    	for(int i : centers_seeds) {
    		System.out.println("NEW CENTER: "+i);
    	}
	}
	
	public static String int_to_string(int[] vec, int div) {
		String str = "";
		
		for(int i=0; i<vec.length; i++) {
			//System.out.println("CHARNUM " + vec[i]/div);
			char app = (char)(Math.round((double)vec[i]/(double)div));
			str += app;
		}
		
		//System.out.println(str);
		return(str);
	}
	
	public static Map<Integer,List<String>> generateFinalClusters() {
		//map the final clusters ( cluster: term)
		Map<Integer,List<String>> clusters = new HashMap<Integer,List<String>>();
		
		for(int j=0; j<num_clusters; j++) {
			clusters.put(j, new ArrayList<String>());
		}
		
		for(int i=0; i<sax.length; i++) {
			clusters.get(assignments[i]).add(sax[i][0]);
		}
		
		return(clusters);
	}
	
	public static Map<Integer,List<Integer>> generateFinalClustersSeeds() {
		//map the final clusters (cluster: node)
		Map<Integer,List<Integer>> clusters = new HashMap<Integer,List<Integer>>();
		
		for(int j=0; j<num_clusters; j++) {
			clusters.put(j, new ArrayList<Integer>());
		}
		
		for(int v : vertices) {
			clusters.get(assignments[vert_to_ind.get(v)]).add(v);
		}
		
		return(clusters);
	}
}
