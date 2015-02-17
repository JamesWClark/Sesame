/*
 * uses http://projects.csail.mit.edu/jwi/
 */

package open.sesame.nlp;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNet {
	
	private IDictionary wordnet;
	
	public WordNet(String pathToDictionary) 
			throws IOException {
		//throw errors or handle here?
		URL url = new URL("file", null, pathToDictionary);
		wordnet = new Dictionary(url);
		wordnet.open();
	}
	
	public IWord getIWord(String word, POS pos) {
		IIndexWord indexWord = wordnet.getIndexWord(word, pos);
		IWordID wordID = indexWord.getWordIDs().get(0);
		IWord iword = wordnet.getWord(wordID);
		return iword;
	}
	
	public IWord getWord(IWordID id) {
		return wordnet.getWord(id);
	}
	
	//should match http://projects.csail.mit.edu/jwi/api/edu/mit/jwi/item/POS.html ???
	public String[] getStems(String[] tokens, String[] tags) {
		WordnetStemmer stemmer = new WordnetStemmer(wordnet);
		String[] stems = new String[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			//stems[i] = stemmer.findStems()
		}
		return stems;
	}
	public List<String> getStems(String word, POS pos) {
		WordnetStemmer stemmer = new WordnetStemmer(wordnet);
		List<String> stems = stemmer.findStems(word, pos);
		return stems;
	}
}
