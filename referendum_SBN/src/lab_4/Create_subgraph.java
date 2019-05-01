package lab_4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang3.ArrayUtils;

import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.GraphWriter;
import it.stilo.g.util.NodesMapper;

public class Create_subgraph {
	public static void main(String[] args) throws IOException {
		
		//load scoreYesNo
		Map<String,long[]> scoreYesNo = new HashMap<String,long[]>();
		Scanner scanner = new Scanner(new File("./data/M_score.csv"));
		scanner.useDelimiter(",");
		while(scanner.hasNextLine()) {
			String[] app = scanner.nextLine().split(",");
			scoreYesNo.put(app[0],new long[] {Long.parseLong(app[1]),Long.parseLong(app[2]),Long.parseLong(app[3])});
		}
		scanner.close();
	
		//Load graph
        FileInputStream fstream = new FileInputStream("./data/Official_SBN-ITA-2016-Net.gz");
        GZIPInputStream gzstream = new GZIPInputStream(fstream);
        InputStreamReader isr = new InputStreamReader(gzstream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);

        //create mapper
        NodesMapper<Long> nodeMapper = new NodesMapper<Long>();

        //read file to get number of nodes and fill mapper
        String line;
        Set<Integer> nodeIds = new HashSet<Integer>();
        while ((line = br.readLine()) != null) {
            String[] app = line.split("\t");
            nodeIds.add(nodeMapper.getId(Long.parseLong(app[0])));
            nodeIds.add(nodeMapper.getId(Long.parseLong(app[1])));
        }
        System.out.println(nodeIds.size());

        //create graph
        WeightedDirectedGraph g = new WeightedDirectedGraph(nodeIds.size() + 1);
        //load graph
        fstream = new FileInputStream("./data/Official_SBN-ITA-2016-Net.gz");
        gzstream = new GZIPInputStream(fstream);
        isr = new InputStreamReader(gzstream, "UTF-8");
        br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
            String[] app = line.split("\t");
            g.add(nodeMapper.getId(Long.parseLong(app[0])), nodeMapper.getId(Long.parseLong(app[1])), Integer.parseInt(app[2]));
        }
        br.close();
        isr.close();
        gzstream.close();
        fstream.close();
        
        
        //create subgraph
        List<Integer> sub_ids = new ArrayList<Integer>();
        for(long[] l : scoreYesNo.values()) {
        	int a = nodeMapper.getId(l[0]);
        	if(nodeIds.contains(a)) {
        		sub_ids.add(a);
        		//System.out.println(a);
        	}
        }
        
        int[] sub_ids2 = ArrayUtils.toPrimitive(sub_ids.toArray(new Integer[sub_ids.size()]));
              
        System.out.println("SUBGRAPH SIZE: " + sub_ids2.length);
        
        WeightedDirectedGraph g1 = SubGraph.extract(g, sub_ids2, 4);
        
        System.out.println("SUBGRAPH SIZE: " + g1.size);
        
        //save subgraph
        GraphWriter.saveDirectGraph(g1, "./data/sub_graph.gz", null);
                
        //Save mapper
        PrintWriter pw = new PrintWriter(new File("./data/node_mapper.csv"));
		for(int v : sub_ids2) {
			StringBuilder sb = new StringBuilder();
			sb.append(nodeMapper.getNode(v)+","+v+"\n");
			pw.write(sb.toString());
		}
		pw.close();
	}
}
