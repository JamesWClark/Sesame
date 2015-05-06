/**
 * QUERY EXAMPLES
 * https://github.com/mongodb/mongo-java-driver/blob/master/driver/src/examples/primer/QueryPrimer.java
 * 
 * JSON EXAMPLE (see note below)
 * 1- http://www.jsonmate.com/permalink/55469b30aa522bae3683ee2c
 * 2- http://jsonmate.com/permalink/554839d0aa522bae3683ee34
 * 
 * Note: Look at example 2 compared to 1, specifically at coreference. Observe there is discrepancy in objects:
 * coreference.coreference.mentions[] != coreference.mentions[]
 * 
 * import static com.mongodb.client.model.Filters.*;
 */

package open.sesame;

import static com.mongodb.client.model.Filters.eq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Main {

	//db stuff
	private static final MongoClient mongo = new MongoClient();
	private static final MongoDatabase db = mongo.getDatabase("yelp");
	private static final MongoCollection<Document> locations = db.getCollection("locations");
	private static final MongoCollection<Document> reviews = db.getCollection("reviews");
	private static final MongoCollection<Document> corenlp = db.getCollection("corenlp");
	
	//for tfidf
	private static Map<String, Integer> tokens = new HashMap<String, Integer>();
	private static Map<String, Integer> docs = new HashMap<String, Integer>();
	private static ArrayList<Integer> tokenColumns = new ArrayList<Integer>();
	private static ArrayList<Integer> documentRows = new ArrayList<Integer>();
	
	//stopwords
	private static Set<String> stopwords = new HashSet<String>();
	
	public static void main(String[] args) {

		loadStopWords("stopwords-long");
		
		//can foreach on categories, but let's stay simple with sandwiches or other for now
		String category = "Sandwiches";
		
		ArrayList<String> categories = getDistinctLocationCategories();
		ArrayList<String> businessIds = getBusinessIdList(category);
		ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
		
		
		tfidf(reviewIds);
		

		mongo.close();
	}
	
	/**
	 * Check if a word or lemma is in the stop word list
	 * @param wordOrLemma the word or lemma to be checked
	 * @return true if yes, false if no
	 */
	static boolean isStopWord(String wordOrLemma) {
		if(stopwords.contains(wordOrLemma))
			return true;
		else 
			return false;
	}
	
	/**
	 * Loads a stop word file into memory
	 * @param fileName Name of the stop word file
	 */
	static void loadStopWords(String fileName) {
		File file = new File(fileName);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       stopwords.add(line);
		    }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Start a TFIDF process on the corenlp result set
	 * @param reviewIdList a list of review IDs from the reviews collection
	 */
	static void tfidf(ArrayList<String> reviewIdList) {
		System.out.print("get review nlp: ");
		BasicDBObject filter = new BasicDBObject("$in", reviewIdList);
		BasicDBObject query = new BasicDBObject("root.document.review_id", filter);
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = corenlp.find(query).iterator();
		Document corejson = cursor.next();
		String json = corejson.toJson();
		
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(json).getAsJsonObject().get("root").getAsJsonObject();
		JsonObject document = root.get("document").getAsJsonObject();
		String review_id = document.get("review_id").getAsString();
		JsonObject sentences = document.get("sentences").getAsJsonObject();
		JsonArray sentence = sentences.get("sentence").getAsJsonArray();
		
		//for one document - eventually replace with a loop
		
		//foreach sentence
		for(int i = 0; i < sentence.size(); i++) {
			JsonObject sentenceIndex = sentence.get(i).getAsJsonObject();
			JsonObject tokens = sentenceIndex.get("tokens").getAsJsonObject();
			JsonArray token = tokens.get("token").getAsJsonArray();
			
			//foreach token
			for(int k = 0; k < token.size(); k++) {
				JsonObject tokenIndex = token.get(k).getAsJsonObject();
				String pos = tokenIndex.get("POS").getAsString();
				String lemma = tokenIndex.get("lemma").getAsString();
				String word = tokenIndex.get("word").getAsString();
				//System.out.println(word + " - " + lemma + " - " + pos);
				
				if(false == isStopWord(lemma)) {
					String represent = lemma + ":;:" + pos;
					incrementTfidf(represent, review_id);
				}
			}
			
			//dependency resolution - we may end up not using this
			JsonArray dependencies = sentenceIndex.get("dependencies").getAsJsonArray();
			for(int m = 0; m < dependencies.size(); m++) {
				JsonObject dependencyIndex = dependencies.get(m).getAsJsonObject();
				JsonArray dep = dependencyIndex.get("dep").getAsJsonArray();
				for(int n = 0; n < dep.size(); n++) {
					JsonObject depIndex = dep.get(n).getAsJsonObject();
					//refer to http://nlp.stanford.edu/software/lex-parser.shtml#Sample
				}
			}
		}
		
		//this problem exists in our data (observed in coref) : 
		//stackoverflow.com/questions/1823264/quickest-way-to-convert-xml-to-json-in-java#answer-15015482
		//the following block is written for json style : http://jsonmate.com/permalink/554839d0aa522bae3683ee34
		
		//coreference resolution
		JsonObject coreferenceObject = document.get("coreference").getAsJsonObject();
		JsonArray coreferenceArray = coreferenceObject.get("coreference").getAsJsonArray();
		for(int i = 0; i < coreferenceArray.size(); i++) {
			JsonObject coreferenceIndex = coreferenceArray.get(i).getAsJsonObject();
			JsonArray mention = coreferenceIndex.get("mention").getAsJsonArray();
			for(int k = 0; k < mention.size(); k++) {
				JsonObject mentionIndex = mention.get(k).getAsJsonObject();
				System.out.println(mentionIndex.get("text").getAsString());
			}
		}
		
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms");
	}
	
	static void incrementTfidf(String token, String document) {
		
	}
	
	/**
	 * Get distinct location categories
	 * @return a list of all the unique categories
	 */
	static ArrayList<String> getDistinctLocationCategories() {
		System.out.print("get distinct categories: ");
		ArrayList<String> list = new ArrayList<String>();
		long start = System.currentTimeMillis();
		MongoCursor<String> categories = locations.distinct("categories", String.class).iterator();
		while(categories.hasNext()) {
			list.add(categories.next().toString());
		}
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
	
	/**
	 * Get a list of business IDs from locations collection
	 * @param category array in locations collection
	 * @return a list of business IDs
	 */
	static ArrayList<String> getBusinessIdList(String category) {
		System.out.print("get business ids: ");
		ArrayList<String> list = new ArrayList<String>();
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = locations.find(eq("categories", category)).iterator();
		while(cursor.hasNext()) {
			list.add(cursor.next().getString("business_id"));
		}
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
	
	/**
	 * Get a list of review IDs from reviews collection, given a list of business IDs
	 * @param businessIdList a list of IDs retrieved from getBusinessIdList() method
	 * @return a list of review IDs
	 */
	static ArrayList<String> getBusinessReviewIdList(ArrayList<String> businessIdList) {
		System.out.print("get review ids: ");
		ArrayList<String> list = new ArrayList<String>();
		BasicDBObject filter = new BasicDBObject("$in", businessIdList);
		BasicDBObject query = new BasicDBObject("business_id", filter);
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = reviews.find(query).iterator();
		while(cursor.hasNext()) {
			list.add(cursor.next().getString("review_id"));
		}
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
}
