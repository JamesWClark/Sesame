/**
 * models
 * http://opennlp.sourceforge.net/models-1.5/
 * 
 * todo: attach source code and/or javadocs
 */

package open.sesame.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;


public class NLPFactory {
	/**
	 * 
	 * @param text
	 * @param modelPath
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static String[] getSentences(String text, String modelPath) throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream(modelPath);
		SentenceModel model = new SentenceModel(is);
		is.close();
		SentenceDetectorME sdetector = new SentenceDetectorME(model);
		String sentences[] = sdetector.sentDetect(text);
		return sentences;
	}

	/**
	 * 
	 * @param text
	 * @param modelPath
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static String[] getTokens(String text, String modelPath)
			throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream(modelPath);
		TokenizerModel model = new TokenizerModel(is);
		is.close();
		TokenizerME tokenizer = new TokenizerME(model);
		String tokens[] = tokenizer.tokenize(text);
		return tokens;
	}

	/**
	 * 
	 * @param text
	 * @param modelPath
	 * @return
	 * @throws IOException
	 */
	public static String[] getNames(String[] text, String modelPath)
			throws IOException {
		InputStream is = new FileInputStream(modelPath);
		TokenNameFinderModel model = new TokenNameFinderModel(is);
		is.close();
		NameFinderME nameFinder = new NameFinderME(model);
		Span nameSpans[] = nameFinder.find(text);
		String[] names = new String[nameSpans.length];
		int count = 0;
		for(Span span : nameSpans) {
			names[count++] = span.toString();
		}
		return names;
	}

	/**
	 * Penn Treebank tags - https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
	 * documentation - http://opennlp.apache.org/documentation/1.5.3/manual/opennlp.html#tools.postagger
	 * @param tokens
	 * @param modelPath
	 * @return
	 * @throws IOException
	 */
	public static String[] getPOS(String[] tokens, String modelPath)
			throws IOException {
		InputStream is =  new FileInputStream(modelPath);
		POSModel model = new POSModel(is);
		is.close();
		POSTaggerME tagger = new POSTaggerME(model);
		String[] tags = tagger.tag(tokens);
		return tags;
	}
	
	/**
	 * documentation - http://opennlp.apache.org/documentation/1.5.3/manual/opennlp.html#tools.chunker
	 * @param tokens
	 * @param tags
	 * @param modelPath
	 * @return
	 * @throws IOException
	 */
	public static String[] getChunks(String[] tokens, String[] tags, String modelPath)
			throws IOException {
		InputStream is = new FileInputStream(modelPath);
		ChunkerModel model = new ChunkerModel(is);
		is.close();
		ChunkerME chunker = new ChunkerME(model);
		return chunker.chunk(tokens, tags);
	}
	
	public static void parse(String text, String modelPath) throws InvalidFormatException, IOException {
		// http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser#Training_Tool
		InputStream is = new FileInputStream(modelPath);
		ParserModel model = new ParserModel(is);
		Parser parser = ParserFactory.create(model);
		Parse topParses[] = ParserTool.parseLine(text, parser, 1);
		for (Parse p : topParses)
			p.show();
		is.close();
	}
}
