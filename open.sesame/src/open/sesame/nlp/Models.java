package open.sesame.nlp;

public class Models {
	private static String pathToModels = System.getProperty("user.dir") + "/models/en/";
    
	public static final String SENTENCE_SEGMENTATION = pathToModels + "en-sent.bin"; 
	public static final String TOKENIZATION = pathToModels + "en-token.bin";
	public static final String POS_TAGGING_MAXENT = pathToModels + "en-pos-maxent.bin";
	public static final String POS_TAGGING_PERCEPTRON = pathToModels + "en-pos-perceptron.bin"; 
	public static final String CHUNKING = pathToModels + "en-chunker.bin";
	public static final String PARSER = pathToModels + "en-parser-chunking.bin";
	public static final String COREFERENCE = pathToModels + "coref";

};  