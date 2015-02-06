/**
 * models
 * http://opennlp.sourceforge.net/models-1.5/
 * 
 * todo: attach source code and/or javadocs
 */

package open.sesame.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
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
	public static String[] detectSentences(String text, String modelPath)
			throws InvalidFormatException, IOException {
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
	public static String[] tokenize(String text, String modelPath)
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
	public static Span[] findNames(String[] text, String modelPath)
			throws IOException {
		InputStream is = new FileInputStream(modelPath);
		TokenNameFinderModel model = new TokenNameFinderModel(is);
		is.close();
		NameFinderME nameFinder = new NameFinderME(model);
		Span nameSpans[] = nameFinder.find(text);
		return nameSpans;
	}

	public static void POSTag(String text, String modelPath) throws IOException {
		POSModel model = new POSModelLoader().load(new File(modelPath));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(text));
		perfMon.start();
		String line;
		while ((line = lineStream.read()) != null) {
			String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
			String[] tags = tagger.tag(whitespaceTokenizerLine);
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			System.out.println(sample.toString());
			perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
	}
	
	public static void chunk(String text, String modelPOSPath, String modelChunkerPath) throws IOException {
		POSModel model = new POSModelLoader().load(new File(modelPOSPath));
		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
		POSTaggerME tagger = new POSTaggerME(model);
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(text));
		perfMon.start();
		String line;
		String whitespaceTokenizerLine[] = null;
		String[] tags = null;
		while ((line = lineStream.read()) != null) {
			whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(line);
			tags = tagger.tag(whitespaceTokenizerLine);
			POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
			System.out.println(sample.toString());
			perfMon.incrementCounter();
		}
		perfMon.stopAndPrintFinalResult();
		// chunker
		InputStream is = new FileInputStream(modelChunkerPath);
		ChunkerModel cModel = new ChunkerModel(is);
		ChunkerME chunkerME = new ChunkerME(cModel);
		String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
		for (String s : result)
			System.out.println(s);
		Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
		for (Span s : span)
			System.out.println(s.toString());
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
