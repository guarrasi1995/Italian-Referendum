package lab_4;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class scraping {
	public static List<String[]> data;
	
	public static void main(String[] argv) throws IOException {
		
		//scrape the 2 websites to get Last name, first name and twitter profiles
		data = new ArrayList<String[]>(scraping.scrape("https://social-media-expert.net/2017/12/elenco-profili-twitter-dei-senatori-italiani/"));
		data.addAll(scraping.scrape("https://social-media-expert.net/2017/12/elenco-profili-twitter-dei-deputati-italiani-xvii-legislatura/"));
		//data = LastName, FirstName, screenname, vote(null)
		
		//sort in alphabetical order
		Collections.sort(data, new Comparator<String[]>(){

			//We do the override to compare the strings arrays by Last Name
		    @Override
		    public int compare(String[] o1, String[] o2) {

		    	return o1[0].compareTo(o2[0]);
		    }
		});
		
		//from the json files get the vote of the politicians
		scraping.loadJSON("./data/Votazione_5.json", "senatore");
		scraping.loadJSON("./data/Votazione_6.json", "deputato"); 
		
		//print the politicians names
		for(String[] i : data) {
			for(String j : i) {
				System.out.print(j + "; ");
			}
			System.out.println();
		}
		
		//How many politicians?
		System.out.println("TOTALE: " + data.size());
		
		//save info in csv (LastName, FirstName, TwitterAccount, Vote)
		scraping.writeCSV();
		
		System.out.println("DONE");
	}

	public static List<String[]> scrape(String url) throws IOException {		
		List<String[]> data = new ArrayList<String[]>();
		
		//set connection
		Document doc = Jsoup.connect(url).get();
		//get tags p
		Elements newsHeadlines = doc.select("p");
		//the 7th tag p has our info, transform back to html and split with <br>
		String[] textSplitResult = newsHeadlines.get(7).html().split("<br>");
		
		//extract the info (LastName, FirstName, TwitterAccount)
		for (String t : textSplitResult) {
			if(t.contains("@")) { //If has screenname->
				String app[] = t.split("–"); //LastName FirstName (party) - screenname
				String cognome = "";
				String nome = "";
				
				for(String str : app[0].split(" ")) {
					if (!str.contains("(")) {
						if(str.toUpperCase().equals(str)) {
							if(cognome.equals(""))
								cognome += str.toUpperCase();
							else
								cognome+=" " + str.toUpperCase();
						} else  {
							if(nome.equals(""))
								nome+=str.toUpperCase();
							else
								nome+=" " + str.toUpperCase();
						}
					}
				}
				
				String screenname = new String();
				for(String str : app)
				{
					if(str.contains("@")) {
						screenname = str.substring(2,str.length()-1); //cut space and @
						break;
					}
				}
				
				data.add(new String[] {cognome, nome, screenname, ""});
			}
		}
		
		return(data);
	}
	

	public static void loadJSON(String filename, String tipo) throws IOException {		
		//use jsonParser to extract info from .json
		JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filename));

            JSONObject arr = (JSONObject) obj;
            JSONArray arguments = (JSONArray) arr.get(tipo);
            //System.out.println("arguments>>>>>>>>> "+arguments);
            
            int j=0;
            int cont = 0;
			//for each politician get the LastName
            for(int i = 0; i<arguments.size(); i++){
                JSONObject object = (JSONObject) arguments.get(i);
                String cognome = ((String) object.get("Cognome")).toUpperCase();
                
                while(true) {
                    //System.out.println(cognome);
                	
					//compare the LastName with the LastName of the other website
                    String cognome2 = data.get(j)[0];
                	int app = cognome.compareTo(cognome2);

					//if there is a match, get the vote
                	if(app==0) {
                		cont++;
                		//System.out.println("MATCH!");
                		//System.out.println(cognome + ", " + data.get(j)[0]);
                		if(data.get(j)[3]!="")
                			System.out.println("CHECK: " + cognome);
                		String vot = ((String) object.get("votazione")).toLowerCase();
						//transform the vote in Y or N and add info to data
                		if(vot.equals("favorevole"))
                		{
                			//System.out.println(vot);
                			data.get(j)[3] = "Y";
                		}
                		else //ASTENUTI = NO
                			//System.out.println(vot);
                			data.get(j)[3] = "N";
                		j++;
                		break;
                	} else if (app>0) {
                		if(j==data.size()-1)
                			break;
                		j++;
                	} else {
                		break;
                	}
                }
            }
			//how many matches?
            System.out.println("MATCHED:" + cont);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void writeCSV() throws FileNotFoundException{
        PrintWriter pw = new PrintWriter(new File("./data/politici.csv"));
        
        //create csv file line by line
        for(String[] i : data) {
        	StringBuilder sb = new StringBuilder();
			for(String j : i) {
				if(j.length()==1)
					sb.append(j);
				else
					sb.append(j + ",");
			}
			sb.append("\n");
			
			pw.write(sb.toString());
		}
        
        pw.close();
    }
}
