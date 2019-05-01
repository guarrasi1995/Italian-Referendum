package lab_4;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.KppNeg;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphReader;

public class HITS_graph {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		//load ScoreYesNo
		//create Long to String map
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
	
		//Load nodeMapper
        Map<Integer,Long> nodeMapper = new HashMap<Integer,Long>();
		scanner = new Scanner(new File("./data/node_mapper.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			nodeMapper.put(Integer.parseInt(app[1]),Long.parseLong(app[0]));
		}
		scanner.close();
	
		//Load graph
		WeightedDirectedGraph g = new WeightedDirectedGraph(Collections.max(nodeMapper.keySet()));
		GraphReader.readGraph(g, "./data/sub_graph.gz", true);

		//Find biggest connected component
		int[] all = new int[g.size];
        for (int k = 0; k < g.size; k++) {
            all[k] = k;
        }
        Set<Set<Integer>> comps = ConnectedComponents.rootedConnectedComponents(g, all, 4);
        
        Set<Integer> max_comp = new HashSet<Integer>();
        int max_size = 0;
        
		for(Set<Integer> c : comps) {
			if(c.size()>max_size) {
				max_comp = c;
				max_size = c.size();
			}
		}
		
		System.out.println(max_size);
		
		//Print connected component
		System.out.println("CONNECTED COMP:");
		for(int k : max_comp) {
			System.out.println(k);
		}
		System.out.println();
        

		//create subgraph of the connected component
        int[] sub_ids = ArrayUtils.toPrimitive(max_comp.toArray(new Integer[max_comp.size()]));
              
        System.out.println("SUBGRAPH SIZE: " + sub_ids.length);
        
        WeightedDirectedGraph g1 = SubGraph.extract(g, sub_ids, 4);
        
		//find top 1000 authorities (YES/NO)
        Map<Integer,double[]> topAuthYes = new HashMap<Integer,double[]>();
        Map<Integer,double[]> topAuthNo = new HashMap<Integer,double[]>();

        ArrayList<ArrayList<DoubleValues>> list;
		list = HubnessAuthority.compute(g1, 0.00001, 1); //graph, threshold, threads
		for (int i = 0; i < list.size(); i++) {
			ArrayList<DoubleValues> score = list.get(i);
			//String x = "";
			if (i == 0) {
				//x = "Auth ";
				for (int j = 0; j < score.size(); j++) {
					long userid = nodeMapper.get(score.get(j).index);
					
					String screenname = userid_to_scrennname.get(userid);
					long[] scores = scoreYesNo.get(screenname);
					
					//YES
					if(scores[1]>scores[2]) {
						if(topAuthYes.size()<1000) {
							topAuthYes.put(score.get(j).index, new double[] {scores[1], score.get(j).value});
						}
					} else { //NO
						if(topAuthNo.size()<1000) {
							topAuthNo.put(score.get(j).index, new double[] {scores[2], score.get(j).value});
						}
					}
					
					//System.out.println( x + score.get(j).value + ":\t\t" + score.get(j).index);
				}
			} else {
				//x = "Hub ";
			}
		}
		
		//Save Auth Yes
        PrintWriter pw = new PrintWriter(new File("./data/topAuth/topAuthYes.csv"));
		for(Integer i : topAuthYes.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topAuthYes.get(i)[0]+","+topAuthYes.get(i)[1]+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		//Save Auth No
        pw = new PrintWriter(new File("./data/topAuth/topAuthNo.csv"));
		for(Integer i : topAuthNo.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topAuthNo.get(i)[0]+","+topAuthNo.get(i)[1]+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		
		//Create combination of authority and score
		Map<Integer,Double> topCombYes = new HashMap<Integer,Double>();
        Map<Integer,Double> topCombNo = new HashMap<Integer,Double>();

		for(Integer i : topAuthYes.keySet()) {
			topCombYes.put(i, topAuthYes.get(i)[0]*topAuthYes.get(i)[1]);
		}
		for(Integer i : topAuthNo.keySet()) {
			topCombNo.put(i, topAuthNo.get(i)[0]*topAuthNo.get(i)[1]);
		}		
		
		
		//Save Combination Yes
        pw = new PrintWriter(new File("./data/topAuth/topCombYes.csv"));
		for(Integer i : topCombYes.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topCombYes.get(i)+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		//Save Combination No
        pw = new PrintWriter(new File("./data/topAuth/topCombNo.csv"));
		for(Integer i : topCombNo.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topCombNo.get(i)+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		
		//KPPNEG
		//Find top 500-Players
		Map<Integer,Double> topKppYes = new HashMap<Integer,Double>();
        Map<Integer,Double> topKppNo = new HashMap<Integer,Double>();

		List<DoubleValues> brokers = KppNeg.searchBroker(g1, g.getVertex(), 4);
		for(DoubleValues b : brokers) {
			long userid = nodeMapper.get(b.index);
			
			String screenname = userid_to_scrennname.get(userid);
			long[] scores = scoreYesNo.get(screenname);
			
			//YES
			if(scores[1]>scores[2]) {
				if(topKppYes.size()<500) {
					topKppYes.put(b.index, b.value);
				}
			} else { //NO
				if(topKppNo.size()<500) {
					topKppNo.put(b.index, b.value);
				}
			}
			System.out.println("Broker value:" + b.value + "\tid:" + b.index);
		}
		
		
		//Save KPPNeg Yes
        pw = new PrintWriter(new File("./data/topAuth/topKYes.csv"));
		for(Integer i : topKppYes.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topKppYes.get(i)+"\n");
			pw.write(sb.toString());
		}
		pw.close();
		
		//Save KPPNeg No
        pw = new PrintWriter(new File("./data/topAuth/topKNo.csv"));
		for(Integer i : topKppNo.keySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.get(i) + ","+topKppNo.get(i)+"\n");
			pw.write(sb.toString());
		}
		pw.close();
	}
}
