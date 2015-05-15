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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import open.sesame.json.buckets.JsonToken;
import open.sesame.tsv.read.TSVReader;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hankcs.lda.Corpus;
import com.hankcs.lda.LdaGibbsSampler;
import com.hankcs.lda.LdaUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Main {
	
	static final int VERSION = 1;
	
	static int totalDocuments = 0;
	static int totalTokens = 0;

	//db stuff
	private static final MongoClient mongo = new MongoClient();
	private static final MongoDatabase db = mongo.getDatabase("yelp");
	private static final MongoCollection<Document> locations = db.getCollection("locations");
	private static final MongoCollection<Document> reviews = db.getCollection("reviews");
	private static final MongoCollection<Document> corenlp = db.getCollection("corenlp");
	
	//for tfidf
	private static Map<String, Token> tokensMap = new HashMap<>();
	private static Map<String, Review> documentsMap = new HashMap<>();
	//json representation of TFIDF
//	private static ArrayList<JsonToken> jsonTokenList = new ArrayList();
	
	//LDA
	private static HashMap<String, Double>[] topicMap;
	
	//stopwords
	private static Set<String> stopwords = new HashSet<String>();
	
	public static void main(String[] args) throws Exception {
		for(String arg : args) {
			switch(args[0]) {
			case "countCategories":
				printReviewCountsByCategory();
				break;
//			case "jsontfidf":
//				JsonParserFactory.readJsonToObjects("C:\\Users\\Jordan\\Documents\\GitHub\\Sesame\\open.sesame\\data\\Afghan.json");
//				
//				break;
			case "tsvread":
				String filepath = "D:\\Yelp-Dataset\\Sesame Out\\Accountants.tsv";
				ldaFromTsv(filepath, 10, 0.65);
				break;
			case "tfidf":
				loadStopWords("stopwords-long");
				ArrayList<String> categories = getDistinctLocationCategories();
				Collections.sort(categories);
				int currentCategory = 0;
				for(String category : categories) {
					category = "Asian Fusion";
					tokensMap = new HashMap<>();
					documentsMap = new HashMap<>();
					ArrayList<String> businessIds = getBusinessIdList(category);
					ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
					totalDocuments = reviewIds.size();
					processTokens(reviewIds, category, currentCategory++, categories.size());
					tfidf(tokensMap, category);
					ArrayList<Token> sortedTfidfTokens = new ArrayList<Token>(tokensMap.values());
					Collections.reverse(sortedTfidfTokens);
					
					/*
					for(int i = 0; i < sortedTfidfTokens.size(); i++) {
						Token t = sortedTfidfTokens.get(i);
						System.out.println(t + ": " + t.tfidf);
					}
					*/
					int p = 5;
					p+=5;
					lda(documentsMap, 12, 0.65);

					
				}
//				break;
//			case "lda":
				lda(documentsMap, 10, 2.0);
				break;
			}
		}

		mongo.close();
	}
	
	/**
	 * Prints review counts by category
	 */
	static void printReviewCountsByCategory() {
		ArrayList<String> categories = getDistinctLocationCategories();
		for(String category : categories) {
			ArrayList<String> businessIds = getBusinessIdList(category);
			ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
			totalDocuments = reviewIds.size();
			System.out.println(category + " : " + totalDocuments);
		}
	}
	
	/**
	 * Checks if an entire string is numeric
	 * @param wordOrLemma String to check
	 * @return true if yes, false if no
	 */
	public static boolean isNumeric(String wordOrLemma) {  
		try {  
			Double.parseDouble(wordOrLemma);  
		} catch(NumberFormatException ex) {  
			return false;  
		}  
		return true;  
	}
	
	/**
	 * Check if a token starts with a single punctuation
	 * @param wordOrLemma A representative of the token to check
	 * @return true if yes, false if no
	 */
	static boolean isPunctuationOrDigit(String wordOrLemma) {
		if(wordOrLemma.length() > 0)
			if(Character.isLetter(wordOrLemma.charAt(0)))
				return false;
			else 
				return true;
		else
			return false;
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
	 * Calculates a TFIDF score for categorical result set from MongoDB
	 * @param tokensMap a HashMap of tokens
	 */
	static void tfidf(Map<String, Token> tokensMap, String category) {
		double maxTF = -1d;
		
		//1st pass through for general tf
		Iterator it = tokensMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry pair = (Entry)it.next();
			Token token = (Token)pair.getValue();
			token.tf = (double)token.count / totalTokens;
			if(token.tf > maxTF)
				maxTF = token.tf;
		}
		
		StringBuilder json = new StringBuilder();
		json.append("{'tfidf':{'version':" + VERSION + ",'category':'" + category + "','tokens':[");
		
		//2nd pass through for augmented tfidf
		it = tokensMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry pair = (Entry)it.next();
			Token token = (Token)pair.getValue();
			token.tf = 0.5 + ((0.5 * token.tf) / maxTF);
			token.idf = Math.log10((double)totalDocuments / token.documents.size());
			token.tfidf = token.tf * token.idf;
			
			JsonToken jt = new JsonToken(token.id, token.tfidf);
			Gson gson = new Gson();
			try {
				String jsonToken = new Gson().toJson(jt);
				json.append(jsonToken);
				if(it.hasNext())
					json.append(",");
			} catch (UnsupportedOperationException ex) {
				//do nothing - this token needs to be skipped
			}				
		}
		json.append("]}}");
		System.out.println(json.toString());
		JsonParser parser = new JsonParser();
		JsonObject jObject = (JsonObject)parser.parse(json.toString());
		
		try {
			PrintWriter writer = new PrintWriter(category + ".json");
			writer.write(jObject.toString());
		} catch (FileNotFoundException ex) {
			//do nothing?
		}
	}
	
	
	/**
	 * Get related topics with Latent Dirichlet Allocation
	 * @param documentsMap a map of document IDs, relating to Yelp reviews
	 * @param K the number of topics in which to group related terms
	 * @param tfidfThreshold the minimum value for below which to exclude tokens from LDA
	 */
	static void lda(Map<String, Review> documentsMap, int K, double tfidfThreshold) {
		Corpus corpus = new Corpus();
		for (String review_id: documentsMap.keySet()) {
			ArrayList<String> document = new ArrayList<String>();
			for(String token_id : documentsMap.get(review_id).tokenIds) {
				Token token = tokensMap.get(token_id);
				if (token.tfidf > tfidfThreshold) {
					document.add(tokensMap.get(token_id).toString());
				}
			}
			if(document.size() > 0)
				corpus.addDocument(document);
		}
		LdaGibbsSampler ldaGibbsSampler = new LdaGibbsSampler(corpus.getDocument(), corpus.getVocabularySize());
		ldaGibbsSampler.gibbs(K);
		double[][] phi = ldaGibbsSampler.getPhi();
		Map<String, Double>[] topicMap = LdaUtil.translate(phi, corpus.getVocabulary(), K);
		LdaUtil.explain(topicMap);
	}
	
	static void ldaFromTsv(String filepath, int K, double tfidfThreshold) {
		Corpus corpus = new Corpus();
		TSVReader tsvReader = new TSVReader(filepath);
		ArrayList<String> document = new ArrayList<String>();
		HashMap<String, ReviewWithTokens> reviewsWithTokens = tsvReader.getReviewsWithTokens();
		for (String review_id: reviewsWithTokens.keySet()) {
			ArrayList<JsonToken> tokens = reviewsWithTokens.get(review_id).getTokens();
			for (JsonToken token: tokens) {
				if (token.getTfidf() > tfidfThreshold) {
					document.add(token.getId());
				}
			}
			if(document.size() > 0)
				corpus.addDocument(document);
		}
		LdaGibbsSampler ldaGibbsSampler = new LdaGibbsSampler(corpus.getDocument(), corpus.getVocabularySize());
		ldaGibbsSampler.gibbs(K);
		double[][] phi = ldaGibbsSampler.getPhi();
		Map<String, Double>[] topicMap = LdaUtil.translate(phi, corpus.getVocabulary(), K);
		LdaUtil.explain(topicMap);
	}
	
	/**
	 * Saving this method for potential use in the future. right now a threshold is constant across the board at 0.6.
	 * However, some reviews might have low TFIDFs or a very large number of tokens.
	 * @param review
	 * @return
	 */
	static double getDocumentTfidfThreshold(Review review) {
		final double thresholdStandard = 0.6;
		double averageTfidf, sumTfidf;
		sumTfidf = 0.0;
		for (String id: review.tokenIds) {
			Token t = tokensMap.get(id);
			sumTfidf += t.tfidf;
		}
		averageTfidf = sumTfidf / review.tokenIds.size();
		return (averageTfidf > thresholdStandard ? averageTfidf : thresholdStandard);
	}
	
	/**
	 * Process token values from the corenlp result set
	 * @param reviewIdList a list of review IDs from the reviews collection
	 */
	static void processTokens(ArrayList<String> reviewIdList, String category, int categoryCount, int totalCategories) {
		System.out.print("get review nlp: ");
		BasicDBObject filter = new BasicDBObject("$in", reviewIdList);
		BasicDBObject query = new BasicDBObject("root.document.review_id", filter);
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = corenlp.find(query).iterator();
		
		//foreach document
		int cursorCount = 0;
		int remaining = totalCategories - categoryCount;
		while(cursor.hasNext()) {
			
			//visual marker in console log
			System.out.println("category: " + category + ", categories togo: " + remaining + ", category progress: " + cursorCount++ + " / " + totalDocuments);
			
			Document corejson = cursor.next();
			String json = corejson.toJson();
			JsonParser parser = new JsonParser();
			JsonObject root = parser.parse(json).getAsJsonObject().get("root").getAsJsonObject();
			JsonObject document = root.get("document").getAsJsonObject();
			JsonObject sentences = document.get("sentences").getAsJsonObject();
			
			String review_id = document.get("review_id").getAsString();
			Review review = new Review(review_id);
			documentsMap.put(review_id, review);
			
			JsonArray sentence = bruteForceJsonArray(sentences, "sentence");
			//foreach sentence
			for(int i = 0; i < sentence.size(); i++) {
				JsonObject sentenceIndex = sentence.get(i).getAsJsonObject();
				JsonObject tokens = sentenceIndex.get("tokens").getAsJsonObject();
				JsonArray token = bruteForceJsonArray(tokens, "token");
				
				//foreach token
				for(int k = 0; k < token.size(); k++) {
					//this UnsupportedOperationException was first thrown in "Asian Fusion" category
					try {
						JsonObject tokenIndex = token.get(k).getAsJsonObject();
						String pos = tokenIndex.get("POS").getAsString();
						String lemma = tokenIndex.get("lemma").getAsString().toLowerCase();
						String word = tokenIndex.get("word").getAsString();
						
						//increment non stop word tokens
						if(!isStopWord(lemma) && !isPunctuationOrDigit(lemma) && !isNumeric(lemma)) {
							String token_id = lemma + ":;:" + pos;
							Token t = tokensMap.get(token_id);
							if(null == t) {
								t = new Token(token_id);
							}
							t.documents.add(review_id);
							t.count++;
							tokensMap.put(token_id, t);
							review.tokenIds.add(token_id);
							totalTokens++;
						}
					} catch (UnsupportedOperationException ex) {
						//try logging it as an error
						try {
							PrintWriter errors = new PrintWriter("errors.log");
							errors.println(ex.getStackTrace());
							errors.println(ex.getMessage());
						} catch (FileNotFoundException ex2) {
							//i guess do nothing ??
						}
					}
				}
				
				/*
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
				*/
			}

			/*
			//coreference resolution
			JsonObject coreferenceObject = document.get("coreference").getAsJsonObject();
			JsonArray coreferenceArray = coreferenceObject.get("coreference").getAsJsonArray();
			for(int i = 0; i < coreferenceArray.size(); i++) {
				JsonObject coreferenceIndex = coreferenceArray.get(i).getAsJsonObject();
				JsonArray mention = coreferenceIndex.get("mention").getAsJsonArray();
				for(int k = 0; k < mention.size(); k++) {
					JsonObject mentionIndex = mention.get(k).getAsJsonObject();
					//System.out.println(mentionIndex.get("text").getAsString());
				}
			}
			*/
		}
		long finish = System.currentTimeMillis();
		System.out.println("elapsed " + (finish - start) + " ms");
	}
	
	/**
	 * Force a JsonObject that should be a JsonArray
	 * @param object the JsonObject to force
	 * @param key they key in the object that will access the value to convert to array
	 * @return a JsonArray data type
	 */
	static JsonArray bruteForceJsonArray(JsonObject jsonObject, String key) {
	    if (jsonObject.get(key).isJsonArray()) {
	        return jsonObject.get(key).getAsJsonArray();
	    } else {
	        JsonArray oneElementArray = new JsonArray();
	        oneElementArray.add(jsonObject.get(key).getAsJsonObject());
	        return oneElementArray;
	    }
	}
	
	/**
	 * Get distinct location categories
	 * @return a list of all the unique categories
	 */
	static ArrayList<String> getDistinctLocationCategories() {
		//System.out.print("get distinct categories: ");
		ArrayList<String> list = new ArrayList<String>();
		long start = System.currentTimeMillis();
		MongoCursor<String> categories = locations.distinct("categories", String.class).iterator();
		while(categories.hasNext()) {
			list.add(categories.next().toString());
		}
		long finish = System.currentTimeMillis();
		//System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
	
	/**
	 * Get a list of business IDs from locations collection
	 * @param category array in locations collection
	 * @return a list of business IDs
	 */
	static ArrayList<String> getBusinessIdList(String category) {
		//System.out.print("get business ids: ");
		ArrayList<String> list = new ArrayList<String>();
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = locations.find(eq("categories", category)).iterator();
		while(cursor.hasNext()) {
			list.add(cursor.next().getString("business_id"));
		}
		long finish = System.currentTimeMillis();
		//System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
	
	/**
	 * Get a list of review IDs from reviews collection, given a list of business IDs
	 * @param businessIdList a list of IDs retrieved from getBusinessIdList() method
	 * @return a list of review IDs
	 */
	static ArrayList<String> getBusinessReviewIdList(ArrayList<String> businessIdList) {
		//System.out.print("get review ids: ");
		ArrayList<String> list = new ArrayList<String>();
		BasicDBObject filter = new BasicDBObject("$in", businessIdList);
		BasicDBObject query = new BasicDBObject("business_id", filter);
		long start = System.currentTimeMillis();
		MongoCursor<Document> cursor = reviews.find(query).iterator();
		while(cursor.hasNext()) {
			list.add(cursor.next().getString("review_id"));
		}
		long finish = System.currentTimeMillis();
		//System.out.println("elapsed " + (finish - start) + " ms, count " + list.size());
		return list;
	}
}