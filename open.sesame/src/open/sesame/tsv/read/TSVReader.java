package open.sesame.tsv.read;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import open.sesame.ReviewWithTokens;
import open.sesame.json.buckets.JsonToken;

public class TSVReader {
	private String filepath;
	public TSVReader(String filepath) {
		this.filepath = filepath;
	}
	
	public HashMap<String, ReviewWithTokens> getReviewsWithTokens(double tfidfThreshold) {
		BufferedReader tsvFile;
		HashMap<String, ReviewWithTokens> documentMap = new HashMap<String, ReviewWithTokens>();
		try {
			tsvFile = new BufferedReader(new FileReader(filepath));
			String line = tsvFile.readLine();
			while (line != null){
				String[] dataArray = line.split("\t");
				String review_id = dataArray[2];
				if (!documentMap.containsKey(review_id)) {
					documentMap.put(dataArray[2], new ReviewWithTokens(review_id));
				}
				double tfidf = Double.parseDouble(dataArray[1]);
				if (tfidf > tfidfThreshold) {
					documentMap.get(review_id).getTokens().add(new JsonToken(dataArray[0], tfidf));
				}
				
				line = tsvFile.readLine(); // Read next line of data.
				}
			tsvFile.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return documentMap;
	}
}
