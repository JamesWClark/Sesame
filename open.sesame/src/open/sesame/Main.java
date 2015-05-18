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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;

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
	static final String CATEGORY_FILE_EXTENSION = ".tsv";
	
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
	
	//LDA
	private static HashMap<String, Double>[] topicMap;
	
	//stopwords
	private static Set<String> stopwords = new HashSet<String>();
	
	public static void main(String[] args) {
		try {
			Args.setArgs(args);
		} catch (UnsupportedOperationException ex) {
			Args.printInstructions();
			System.exit(0);
		}
		
		loadStopWords("stopwords-long");
		ArrayList<String> categories = getDistinctLocationCategories();
		ArrayList<File> filesInWorkingDirectory = new ArrayList<File>(Arrays.asList(new File(System.getProperty("user.dir")).listFiles()));
		Collections.sort(categories);
		
		int currentCategory = 0;
		if(Args.countCategories) {
			printReviewCountsByCategory();
		} else if(Args.tfidf) {
			currentCategory = 0;
			if(Args.singleCategory) {
				String category = Args.category;
				tokensMap = new HashMap<>();
				documentsMap = new HashMap<>();
				ArrayList<String> businessIds = getBusinessIdList(category);
				ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
				totalDocuments = reviewIds.size();
				processTokens(reviewIds, category, currentCategory, categories.size());
				tfidf(tokensMap, category);
				ArrayList<Token> sortedTfidfTokens = new ArrayList<Token>(tokensMap.values());
				Collections.sort(sortedTfidfTokens);
				writeTokensToFile(sortedTfidfTokens, category);
			} else {
				for(String category : categories) {
					currentCategory++;
					if(false == categoryFileExists(filesInWorkingDirectory, category)) {
						tokensMap = new HashMap<>();
						documentsMap = new HashMap<>();
						ArrayList<String> businessIds = getBusinessIdList(category);
						ArrayList<String> reviewIds = getBusinessReviewIdList(businessIds);
						totalDocuments = reviewIds.size();
						processTokens(reviewIds, category, currentCategory, categories.size());
						tfidf(tokensMap, category);
						ArrayList<Token> sortedTfidfTokens = new ArrayList<Token>(tokensMap.values());
						Collections.sort(sortedTfidfTokens);
						writeTokensToFile(sortedTfidfTokens, category);
					}
				}
			}
		} else if(Args.lda) {
			int K = Args.K;
			double threshold = Args.threshold;
			currentCategory = 0;
			if(Args.singleCategory) {
				String category = Args.category;
				if(true == categoryFileExists(filesInWorkingDirectory, category)) {
					currentCategory++;
					documentsMap = loadDocumentsMapFromFile(category + CATEGORY_FILE_EXTENSION, threshold);
					lda(documentsMap, K);
				}
			} else {
				for(String category : categories) {
					if(true == categoryFileExists(filesInWorkingDirectory, category)) {
						currentCategory++;
						documentsMap = loadDocumentsMapFromFile(category + CATEGORY_FILE_EXTENSION, threshold);
						lda(documentsMap, K);
					}
				}
			}
		}
		mongo.close();
	}
	
	/**
	 * Loads a TSV file into documentsMap - accounts for threshold
	 * @param fileName
	 * @param threshold
	 * @return
	 */
	static Map<String, Review> loadDocumentsMapFromFile(String fileName, double threshold) {
		Map<String, Review> tempMap = new HashMap<>();
		try {
			String line;
		    InputStream fis = new FileInputStream(fileName);
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		    while ((line = br.readLine()) != null) {
		    	String[] stuff = line.split("\t");
		    	String token_id = stuff[0];
		    	double score = Double.parseDouble(stuff[1]);
		    	if(score >= threshold) {
			    	String[] review_ids = stuff[2].split(",");
			    	for(int i = 0; i < review_ids.length; i++) {
			    		String review_id = review_ids[i];
			    		if(tempMap.containsKey(review_id)) {
			    			Review review = tempMap.get(review_id);
			    			review.tokenIds.add(token_id);
			    		} else {
			    			Review review = new Review(review_ids[i]);
				    		review.tokenIds.add(token_id);
				    		tempMap.put(review_id, review);
			    		}
			    	}
		    	}
		    }
		    return tempMap;
		} catch (IOException ex) {
			return null;
		}
	}
	
	/**
	 * Check for existence of a previously processed category result file
	 * @param files
	 * @param category
	 * @return
	 */
	static boolean categoryFileExists(ArrayList<File> files, String category) {
		for(File f : files) {
			if(f.getName().equals(category + CATEGORY_FILE_EXTENSION)) {
				return true;
			}
		}
		return false;
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
	 * Checks for words that include more than 1 hyphen
	 * @param wordOrLemma
	 * @return
	 */
	static boolean isHyphenatedSeries(String wordOrLemma) {
		if(wordOrLemma.split("-").length > 1)
			return true;
		else
			return false;
	}
	
	/**
	 * Checks if a token is a web address
	 * @param wordOrLemma
	 * @return
	 */
	static boolean isURL(String wordOrLemma) {
		if(wordOrLemma.indexOf("http://") == -1 && wordOrLemma.indexOf("https://") == -1)
			return false;
		else
			return true;
	}
	
	/**
	 * Checks if an entire string is numeric
	 * @param wordOrLemma String to check
	 * @return true if yes, false if no
	 */
	static boolean isNumeric(String wordOrLemma) {  
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

		//2nd pass through for augmented tfidf
		it = tokensMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry pair = (Entry)it.next();
			Token token = (Token)pair.getValue();
			token.tf = 0.5 + ((0.5 * token.tf) / maxTF);
			token.idf = Math.log10((double)totalDocuments / token.documents.size());
			token.tfidf = token.tf * token.idf;
		}
	}
	
	/**
	 * For use with inline TFIDF result: Get related topics with Latent Dirichlet Allocation
	 * @param documentsMap a map of document IDs, relating to Yelp reviews
	 * @param K the number of topics in which to group related terms
	 * @param tfidfThreshold the minimum value for below which to exclude tokens from LDA
	 */
	static void inlinelda(Map<String, Review> documentsMap, int K, double tfidfThreshold) {
		Corpus corpus = new Corpus();
		for (String review_id: documentsMap.keySet()) {
			ArrayList<String> document = new ArrayList<String>();
			for(String token_id : documentsMap.get(review_id).tokenIds) {
				document.add(tokensMap.get(token_id).toString());
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
	 * For use on a preprocessed TFIDF saved to TSV file
	 * @param documentsMap
	 * @param K
	 * @param tfidfThreshold
	 */
	static void lda(Map<String, Review> documentsMap, int K) {
		Corpus corpus = new Corpus();
		for (String review_id: documentsMap.keySet()) {
			ArrayList<String> document = new ArrayList<String>();
			for(String token_id : documentsMap.get(review_id).tokenIds) {
				document.add(token_id);
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
						if(!isStopWord(lemma) && !isPunctuationOrDigit(lemma) && !isNumeric(lemma) && !isURL(lemma) && !isHyphenatedSeries(lemma)) {
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
							PrintWriter errors = new PrintWriter(new FileOutputStream(new File("errors.log")), true);
							errors.println(ex.getStackTrace());
							errors.println(ex.getMessage());
							errors.close();
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
	 * Prints a categorical token tfidf score to TSV format
	 * @param sortedTfidfTokens a descending sorted list of tokens
	 * @param category the category of business
	 */
	static void writeTokensToFile(ArrayList<Token> sortedTfidfTokens, String category) {
		try {
			PrintWriter tsv = new PrintWriter(category + CATEGORY_FILE_EXTENSION);	
			for(int i = 0; i < sortedTfidfTokens.size(); i++) {
				Token token = sortedTfidfTokens.get(i);
				tsv.print(token.id + "\t" + token.tfidf + "\t");
				Iterator it = token.documents.iterator();
				while (it.hasNext()) {
				    tsv.print(it.next());
				    if(it.hasNext())
				    	tsv.print(",");
				}
				tsv.print("\n");
			}
			tsv.close();
		} catch (FileNotFoundException ex) {
			System.out.println("caught file not found exception in print tfidf method");
			System.exit(0);
		}
		
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
