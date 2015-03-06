package open.sesame;

import java.io.IOException;
import java.util.Scanner;

import open.sesame.opennlp.Models;
import open.sesame.opennlp.NLPFactory;
import opennlp.tools.coref.DiscourseEntity;

public class Main {
	
	public static final String WORDNET_PATH = "C:/Program Files (x86)/WordNet/3.0/dict";

	private static Scanner scanner = null;		

	public static void main(String[] args) throws IOException {
		

		/*
		//let args[0] = path to input file
		if(args.length > 0) {
			String inputPath = args[0];
			System.out.println("Attempting to read from: " + args[0]);
			new TSVWriter(new File(inputPath));
		} else {
			new SwingGUI();
		}
		*/
		/* COREF */
		String sentence = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.";

		DiscourseEntity entity = NLPFactory.coref(sentence, Models.COREFERENCE)[0];
		System.out.println(entity.toString());
		
		
		
		/* PARSER
		//where is the document analyzer model ???
		String modelPath = MODELS_DIRECTORY + "en-parser-chunking.bin";
		String inputText = args[0];
		*/

		/* doc level analysis requires models we do not yet have - maybe we need to train them
		DocumentCategorizerME myCategorizer = new DocumentCategorierME(m);
		double[] outcomes = myCategorizer.categorize(inputText);
		String category = myCategorizer.getBestOutcome();
		*?
		
		
		//String modelPath = MODELS_DIRECTORY + "en-parser-chunking.bin";
		//System.out.println(NLPFactory.parse(args[0], modelPath));

		/* WORDNET
		WordNet wordnet = new WordNet(WORDNET_PATH);
		String word = "therapy";
		POS pos = POS.NOUN;
		for(String s : wordnet.getStems(word, pos)) {
			System.out.println(s);
			IWord iword = wordnet.getIWord(s, pos);
			for(IWordID id : iword.getRelatedWords()) {
				System.out.print(wordnet.getWord(id).getLemma() + ", ");
			}
			System.out.println(iword.getLemma());	
		}
		
		
		/*
		List<String> stems = wordnet.getStems("cleaned", POS.VERB);

		for(String s : stems) {
			System.out.println(s);
		}
		
		/*
		String document = args[0];
		
		try {
			String[] sentences = NLPFactory.getSentences(document, MODELS_DIRECTORY + "en-sent.bin");
			int count = 1;
			for(String s : sentences)
				System.out.println("Sentence " + count++ + ": " + s);

			/*
			String[] tokens = NLPFactory.getTokens(sentences[sentenceIndex], MODELS_DIRECTORY + "en-token.bin");
			for(String s : tokens) {
				System.out.print(s + ", ");
			}
			String[] tags = NLPFactory.getPOS(tokens, MODELS_DIRECTORY + "en-pos-maxent.bin");
			String[] names = NLPFactory.getNames(tokens, MODELS_DIRECTORY + "en-ner-person.bin");
			String[] chunks = NLPFactory.getChunks(tokens, tags, MODELS_DIRECTORY + "en-chunker.bin");
			printTokenTable(tokens, tags, chunks);
			printNames(names);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}*/
		
	}
}
