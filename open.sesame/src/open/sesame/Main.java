/**
 * QUERY EXAMPLES
 * https://github.com/mongodb/mongo-java-driver/blob/master/driver/src/examples/primer/QueryPrimer.java
 * 
 * JSON EXAMPLE
 * http://www.jsonmate.com/permalink/55469b30aa522bae3683ee2c
 * 
 * import static com.mongodb.client.model.Filters.*;
 */

package open.sesame;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;

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

	private static final MongoClient mongo = new MongoClient();
	private static final MongoDatabase db = mongo.getDatabase("yelp");
	private static final MongoCollection<Document> locations = db.getCollection("locations");
	private static final MongoCollection<Document> reviews = db.getCollection("reviews");
	private static final MongoCollection<Document> corenlp = db.getCollection("corenlp");
	
	public static void main(String[] args) {
		
		//can foreach on categories, but let's stay simple with sandwiches or other for now
		String category = "Sandwiches";
		
		ArrayList<String> categories = getDistinctLocationCategories();
		ArrayList<String> businessIds = getBusinessIdList(category);
		ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
		
		
		tfidf(reviewIds);
		

		mongo.close();
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
		JsonObject sentences = document.get("sentences").getAsJsonObject();
		JsonArray sentence = sentences.get("sentence").getAsJsonArray();
		for(int i = 0; i < sentence.size(); i++) {
			JsonObject sentenceIndex = sentence.get(i).getAsJsonObject();
			JsonObject tokens = sentenceIndex.get("tokens").getAsJsonObject();
			JsonArray token = tokens.get("token").getAsJsonArray();
			for(int k = 0; k < token.size(); k++) {
				JsonObject tokenIndex = token.get(k).getAsJsonObject();
				String pos = tokenIndex.get("POS").getAsString();
				String lemma = tokenIndex.get("lemma").getAsString();
				String word = tokenIndex.get("word").getAsString();
				System.out.println(word + " - " + lemma + " - " + pos);
			}
		}
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms");
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
