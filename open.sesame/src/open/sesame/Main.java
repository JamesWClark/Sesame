package open.sesame;

import java.io.IOException;

import open.sesame.nlp.NLPFactory;
import opennlp.tools.util.InvalidFormatException;


public class Main {
	
	private static String MODELS_DIR = System.getProperty("user.dir") + "/models/en/";

	public static void main(String[] args) throws InvalidFormatException, IOException {
		String text = "hey, what's up? how are you? my name is James. this is open nlp don't you know?";

		
		//sentences
		String modelPath = MODELS_DIR + "en-sent.bin";
		for(String s : NLPFactory.detectSentences(text, modelPath)) {
			System.out.println(s);
		}
		/*
		//tokens
		modelPath = MODELS_DIR + "en-token.bin";
		String[] tokens = NLPFactory.tokenize(text, modelPath);
		for(String s : tokens) {
			System.out.println(s);
		}
		
		//names
		modelPath = MODELS_DIR + "en-ner-person.bin";
		for(Span s : NLPFactory.findNames(tokens, modelPath)) {
			System.out.println(s.toString());
		}
		
		//pos
		modelPath = MODELS_DIR + "en-pos-maxent.bin";
		NLPFactory.POSTag(text, modelPath);
		*/
		String modelPOSPath = MODELS_DIR + "en-pos-maxent.bin";
		String modelChunkerPath = MODELS_DIR + "en-chunker.bin";
		NLPFactory.chunk(text, modelPOSPath, modelChunkerPath);
		
		modelPath = MODELS_DIR + "en-parser-chunking.bin";
		NLPFactory.parse(text, modelPath);

	}

}
