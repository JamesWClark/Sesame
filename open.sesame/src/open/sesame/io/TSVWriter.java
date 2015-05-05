package open.sesame.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import open.sesame.Main;
import open.sesame.opennlp.Models;
import open.sesame.opennlp.NLPFactory;
import open.sesame.opennlp.PennTreebankPOS;
import open.sesame.opennlp.StopWords;
import open.sesame.opennlp.WordNet;
import opennlp.tools.util.Span;
import edu.mit.jwi.item.POS;

public class TSVWriter {

 	public static final String WORDNET_PATH = "C:/Program Files (x86)/WordNet/3.0/dict";
	
	StopWords stopwords = new StopWords();
	//gonna need a dictionary for lemma lookups
	WordNet wordnet = new WordNet(WORDNET_PATH);
	//the dictionary needs a simplified list of POS tags (noun, verb, adjective, adverb)
	HashMap<String, POS> wordnetPOSMap = PennTreebankPOS.getWordNetAssociationsMap();
	
	String TAB = "\t";
	String NEWLINE = "\n";
	
	public TSVWriter(File lines) throws IOException {
		
		System.out.println("TSVWriter instantiated");
		
		//reader
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(lines)));
		scanner.useDelimiter("\n");
		
		//writer
		File output = new File("results.tsv");
		PrintWriter writer = new PrintWriter(output);
		
		//document stuff
		String document;
		String[] sentences, tokens, tags;
		Span[] chunks;

		while(scanner.hasNext()) {
			document = scanner.next();
			document = document.replace("\"", "");
			sentences = NLPFactory.getSentences(document, Models.SENTENCE_SEGMENTATION);
			
			writer.write(document.trim() + NEWLINE + NEWLINE);
			System.out.println("document: " + document);
			
			for(String sentence : sentences) {
				sentence = sentence.trim();
				tokens = NLPFactory.getTokens(sentence, Models.TOKENIZATION);
				tags = NLPFactory.getPOS(tokens, Models.POS_TAGGING_MAXENT);
				chunks = NLPFactory.getChunkSpans(tokens, tags, Models.CHUNKING);
				
				System.out.println("sentence: " + sentence);
				System.out.println("tokens: " + tokens.toString());
				System.out.println("tags: " + tags.toString());
				System.out.println("chunks: " + chunks.toString());

				writer.write(sentence + NEWLINE + NEWLINE);
				writer.write("Tokens" + TAB + "Treebank Tags" + TAB + "WordNet Tags" + TAB + "Stems" + NEWLINE);
				
				//build two lists of tokens and tags without stop words
				//by removing stop words after tagging, we get more accurate parts of speech (as seen in their original context)
				//need some dynamic sizing data structures to store modified lists
				ArrayList<String> listTokens = new ArrayList<String>();
				ArrayList<String> listTags = new ArrayList<String>();
				ArrayList<String> listStems = new ArrayList<String>();
								
				//add words that are NOT stop words, and add their matching tags too
				for(int i = 0; i < tokens.length; i++) {
					if(!stopwords.contains(tokens[i])) {
						listTokens.add(tokens[i]);
						listTags.add(tags[i]);
					}
				}
				
				//for every tagged token, find its corresponding wordnet tag and look for lemmas
				for(int i = 0; i < listTokens.size(); i++) {
					POS wordnetTag = wordnetPOSMap.get(listTags.get(i));
					if(wordnetTag == null)
						listStems.add("");
					else
						listStems.add(wordnet.getStems(listTokens.get(i), wordnetTag).toString());
				}
				
				//write columns
				for(int i = 0; i < listTokens.size(); i++) {
					writer.write(listTokens.get(i) + TAB + listTags.get(i) + TAB + wordnetPOSMap.get(tags[i]) + TAB + listStems.get(i) + NEWLINE);
				}
				writer.write(NEWLINE + NEWLINE);
			}
			writer.write(NEWLINE);
		}
		
		scanner.close();
		writer.close();
	}
}
