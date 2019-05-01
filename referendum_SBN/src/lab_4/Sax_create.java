package lab_4;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Sax_create {
	public static void main(String[] args) throws Exception {
		//load tweets(Y/N)
		LoadTweets.load_LUCENE_tweets0("./data/tweets_politici");
		
		//create for the top 1000 (Y/N) words the sax strings and save to file
		LoadTweets.top_terms_to_sax(15);
		
		
		
		//Load sax strings from file
		String[][] sax_Y = new String[1000][2];
		String[][] sax_N = new String[1000][2];
		
		//Load YES Sax strings
		Scanner scanner = new Scanner(new File("./data/saxY.csv"));
		scanner.useDelimiter(",");
		int cont = 0;
		while(scanner.hasNextLine()) {
			sax_Y[cont] = scanner.nextLine().split(",");
			cont++;
		}
		scanner.close();
		
		//Load NO Sax strings
		scanner = new Scanner(new File("./data/saxN.csv"));
		scanner.useDelimiter(",");
		cont=0;
		while(scanner.hasNextLine()) {
			sax_N[cont] = scanner.nextLine().split(",");
			cont++;
		}
		scanner.close();
		
		
		//FOR YES: Apply k-means (2 clusters) (100 max iterations, 15 alphabet size)
		int num_clusters = 2;
		Map<Integer,List<String>> clusters = Algorithms.k_means_sax(sax_Y, num_clusters, 100, 15);
		//for each cluster create csv file (word for each line)
		for(int j=0; j<num_clusters; j++) {
			PrintWriter pw = new PrintWriter(new File("./data/clusters_words/clusters_yes/cluster"+j+".csv"));
			List<String> app = clusters.get(j);
			System.out.println("CLUSTER "+j);
			for(int i=0; i<app.size(); i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(app.get(i)+"\n");
				pw.write(sb.toString());
				System.out.print(app.get(i)+"; ");
			}
			System.out.println();
			pw.close();
		}
		
		//FOR NO: Apply k-means (2 clusters) (100 max iterations, 15 alphabet size)
		num_clusters = 2;
		clusters = Algorithms.k_means_sax(sax_N, num_clusters, 100, 15);
		//for each cluster create csv file (word for each line)
		for(int j=0; j<num_clusters; j++) {
			PrintWriter pw = new PrintWriter(new File("./data/clusters_words/clusters_no/cluster"+j+".csv"));
			List<String> app = clusters.get(j);
			System.out.println("CLUSTER "+j);
			for(int i=0; i<app.size(); i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(app.get(i)+"\n");
				pw.write(sb.toString());
				System.out.print(app.get(i)+"; ");
			}
			System.out.println();
			pw.close();
		}
		
		System.out.println("DONE");
	}
}
