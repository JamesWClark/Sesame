/**
 * models
 * http://opennlp.sourceforge.net/models-1.5/
 * 
 * todo: attach source code and/or javadocs
 */

package open.sesame.opennlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
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
	
	/**
	 * 
	 * @param tokens
	 * @param tags
	 * @param modelPath
	 * @return
	 * @throws IOException
	 */
	public static Span[] getChunkSpans(String[] tokens, String[] tags, String modelPath)
			throws IOException {
		InputStream is = new FileInputStream(modelPath);
		ChunkerModel model = new ChunkerModel(is);
		is.close();
		ChunkerME chunker = new ChunkerME(model);
		Span[] spans = chunker.chunkAsSpans(tokens, tags);
		return spans;
	}
	
	/**
	 * 
	 * @param text
	 * @param modelPath
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static String parse(String text, String modelPath) throws InvalidFormatException, IOException {
		// http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser#Training_Tool
		InputStream is = new FileInputStream(modelPath);
		ParserModel model = new ParserModel(is);
		is.close();
		Parser parser = ParserFactory.create(model);
		Parse topParses[] = ParserTool.parseLine(text, parser, 1);
		for (Parse p : topParses) {
			p.show();
		}
		return topParses[0].toString();
	}
	
	/**
	 * http://blog.dpdearing.com/2012/11/making-coreference-resolution-with-opennlp-1-5-0-your-bitch/#update1
	 * @param sentence
	 * @param modelPath
	 * @throws IOException
	 */
	public static DiscourseEntity[] coref(String sentence, String modelPath) 
			throws IOException {
		List<Mention> document = new ArrayList<Mention>();
		Linker linker = new DefaultLinker(modelPath, LinkerMode.TEST);
		InputStream is = new FileInputStream(Models.PARSER);
		ParserModel model = new ParserModel(is);
		is.close();
		Parser parser = ParserFactory.create(model);
		Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);
		Parse parse = topParses[0];
		DefaultParse parseWrapper = new DefaultParse(parse, 0);
		Mention[] mentions = linker.getMentionFinder().getMentions(parseWrapper);
		for(int i = 0; i < mentions.length; i++) {
			if(mentions[i].getParse() == null) {
				Parse snp = new Parse(parse.getText(), mentions[i].getSpan(), "NML", 1.0, 0);
				parse.insert(snp);
				mentions[i].setParse(new DefaultParse(snp, i));
			}
		}
		document.addAll(Arrays.asList(mentions));
		if (!document.isEmpty()) {
			return linker.getEntities(document.toArray(new Mention[0]));
		}
		return new DiscourseEntity[0];
	}
}
