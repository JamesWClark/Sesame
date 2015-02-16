package open.sesame.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import open.sesame.nlp.Models;
import open.sesame.nlp.NLPFactory;
import opennlp.tools.util.Span;

public class TSVWriter {
	
	private boolean headerWritingEnabled = true;

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
		
		//write the header row
		if(headerWritingEnabled) {
			writer.write("Document" + TAB + "Sentences" + TAB + "Tokens" + TAB + "POS Tags" + TAB + "Chunks" + NEWLINE);
		}

		while(scanner.hasNext()) {
			document = scanner.next();
			sentences = NLPFactory.getSentences(document, Models.SENTENCE_SEGMENTATION);	
			
			writer.write(document.trim());
			System.out.println("document: " + document);
			
			boolean firstSentence = true;
			for(String sentence : sentences) {
				sentence = sentence.trim();
				tokens = NLPFactory.getTokens(sentence, Models.TOKENIZATION);
				tags = NLPFactory.getPOS(tokens, Models.POS_TAGGING_MAXENT);
				chunks = NLPFactory.getChunkSpans(tokens, tags, Models.CHUNKING);
				
				System.out.println("sentence: " + sentence);
				System.out.println("tokens: " + tokens.toString());
				System.out.println("tags: " + tags.toString());
				System.out.println("chunks: " + chunks.toString());
				
				if(firstSentence) { // indent 1 cell (document occupies column 1)
					writer.write(TAB + sentence); 
					firstSentence = false;
				} else { // indent 2 cells on a new row (leave a blank space in previous column)
					writer.write(NEWLINE + TAB + sentence);
				}
				
				writer.write(TAB + arrayToCSV(tokens));
				writer.write(TAB + arrayToCSV(tags));
			}
			writer.write(NEWLINE);
		}
		
		scanner.close();
		writer.close();
	}
	private String arrayToCSV(String[] array) {
		String s = "";
		if(array.length > 0) {
			for(int i = 0; i < array.length - 1; i++) {
				s += array[i] + ", "; 
			}
			s += array[array.length - 1];
		}
		return s;
	}
}
