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

public class LPA {
	public static void main(String[] args) throws IOException {
		//Load mapper and create reverse mapper
		Map<Integer,Long> nodeMapper = new HashMap<Integer,Long>();
        Map<Long,Integer> nodeMapperReverse = new HashMap<Long,Integer>();

        Scanner scanner = new Scanner(new File("./data/node_mapper.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			nodeMapper.put(Integer.parseInt(app[1]),Long.parseLong(app[0]));
			nodeMapperReverse.put(Long.parseLong(app[0]),Integer.parseInt(app[1]));
		}
		scanner.close();
		

		//load graph
		WeightedDirectedGraph g = new WeightedDirectedGraph(Collections.max(nodeMapper.keySet()));
		GraphReader.readGraph(g, "./data/sub_graph.gz", true);

        int[] vert = g.getVertex();
        
        //Load K-players
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
        
		
		
		//Initialize array for LPA results
		int[][] result = new int[10][g.size];
		//Run LPA 10 times
        for(int i=0; i<10; i++) {
	        result[i] = ModComunityLPA.compute(g, seeds[0], seeds[1], 1/vert.length, 1);
        }
        
        //Print results of LPA
        /*for(int i=0; i<result.length; i++) {
        	for(int j=0; j<result[i].length; j++) {
        		System.out.println(result[i][j]);
        	}
        }*/
        
        //Find most frequent label for each node
        int[] mean_result = new int[vert.length];
        int cont=0;
        for(int v : vert) {
        	int[] app = new int[10];

	        for(int i=0; i<10; i++) {
		        app[i] = result[i][v];
		        //System.out.println(result[i][v]);
	        }
	        
	        //get frequency
        	Map<Integer,Integer> integersCount = new HashMap<Integer,Integer>();
	        for (int i : app) {
        	    if (!integersCount.containsKey(i))
        	        integersCount.put(i, 1);
        	    else
        	        integersCount.put(i, integersCount.get(i) + 1);
    	    }
	        
	        //find max frequency
	        int maxFreq = 0;
	        int maxLabel = -1;
	        
	        for(int i : integersCount.keySet()) {
	        	if(integersCount.get(i)>maxFreq)
	        	{
	        		maxFreq = integersCount.get(i);
	        		maxLabel = i;
	        	}
	        }	        
	        
	        mean_result[cont] = maxLabel;
	        cont++;
        }
        
        //Save most frequent label
        PrintWriter pw = new PrintWriter(new File("./data/LPA_labels.csv"));
        cont=0;
        for(int v : vert) {
        	StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(v)+","+mean_result[cont]+"\n");
			pw.write(sb.toString());
			cont++;
        }
        pw.close();
        
        
        
        
        //Compute NMI Matrix between the 10 LPA runs
        double[][] NMI_matrix = new double[10][10];
        NMI_matrix[0][0] = 1;
        for(int i=1; i<10; i++) {
        	NMI_matrix[i][i] = 1;
        	int[] cl1 = result[i];
        	for(int j=0; j<i; j++) {
        		int[] cl2 = result[j];
        		double app = NMI(cl1, cl2);
        		NMI_matrix[i][j] = app;
        		NMI_matrix[j][i] = app;
        	}
        }
 
        
        //Print matrix and save to file
        pw = new PrintWriter(new File("./data/NMI_matrix.csv"));
        for(int i=0; i<10; i++) {
        	StringBuilder sb = new StringBuilder();
        	for(int j=0; j<10; j++) {
        		System.out.printf("%.2f ", NMI_matrix[i][j]);
        		if(j==9)
        			sb.append(NMI_matrix[i][j]+"\n");
        		else
        			sb.append(NMI_matrix[i][j]+",");
        	}
        	System.out.println();
        	pw.write(sb.toString());
        }
        pw.close();
	}
	
	public static double NMI(int[] a, int[] b) {
		int n=0; //Number of nodes
		//Get number of nodes for clusters in first LPA
		Map<Integer, Integer> clusters_a = new HashMap<Integer,Integer>();
		for(int l : a) {
			if(l!=-1) {
				n++;
	     	    if (!clusters_a.containsKey(l))
	     	    	clusters_a.put(l, 1);
	     	    else
	     	    	clusters_a.put(l, clusters_a.get(l) + 1);
			}
 	    }
		
		//Print frequency
    	System.out.print("A: "); 
		for(int i : clusters_a.keySet()) {
        	System.out.println(i+": "+clusters_a.get(i));
        }
    	System.out.println();

    	//Get number of nodes for clusters in second LPA
		Map<Integer, Integer> clusters_b = new HashMap<Integer,Integer>();
		for(int l : b) {
			if(l!=-1) {
	     	    if (!clusters_b.containsKey(l))
	     	    	clusters_b.put(l, 1);
	     	    else
	     	    	clusters_b.put(l, clusters_b.get(l) + 1);
			}
 	    }
	       
		//Print frequency
		System.out.print("B: "); 
		for(int i : clusters_b.keySet()) {
        	System.out.println(i+": "+clusters_b.get(i));
        }
    	System.out.println();
		
    	//Get number of shared nodes between clusters in first and second LPA
		Map<String, Integer> shared = new HashMap<String, Integer>();
		for(int i : clusters_a.keySet()) {
			for(int j : clusters_b.keySet()) {
				String key = i+","+j;
				shared.put(key, 0);
			}
		}
		
		for(int i=0; i<a.length; i++) {
			if(a[i]!=-1) {
				String key =a[i]+","+b[i];
     	    	shared.put(key, shared.get(key) + 1);     	    	
			}
		}
		

		//Print shared nodes frequencies
		System.out.print("AB: "); 
		for(int i : clusters_a.keySet()) {
			for(int j : clusters_b.keySet()) {
	        	System.out.print(shared.get(i+","+j)+" ");
	        }
			System.out.println();
		}
    	System.out.println();
		
		
		//Compute NMI
    	//numerator
		//int na = clusters_a.size();
		//int nb = clusters_b.size();
		double num = 0;
		for(int i : clusters_a.keySet()) {
			for(int j : clusters_b.keySet()) {
				String key = i+","+j;
				num+=shared.get(key)*Math.log((n*shared.get(key)+1)/((double)clusters_a.get(i)*clusters_b.get(j)));
			}
		}
    	//System.out.println(num);

    	//denominator
		double den_a = 0;
		for(int i : clusters_a.keySet()) {
			den_a+=clusters_a.get(i)*Math.log(((double)clusters_a.get(i))/n);
		}
    	//System.out.println(den_a); //

		
		double den_b = 0;
		for(int i : clusters_b.keySet()) {
			den_b+=clusters_b.get(i)*Math.log(((double)clusters_b.get(i))/n);
		}
    	//System.out.println(den_b); //

		//numerator/denominator
		double den = Math.sqrt(den_a*den_b);
    	//System.out.println(den); //

		System.out.println();
		System.out.println();
		System.out.println();
		return(num/den);
	}
}
