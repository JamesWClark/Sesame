/*
 * http://www.ranks.nl/stopwords
 */

package open.sesame.nlp;

import java.util.ArrayList;

public class StopWords {
	
	ArrayList<String> list;
	
	public StopWords() {
		list = new ArrayList<String>();
		buildDefaultEnglishList();
	}
	
	/**
	 * Check if the specified stop word list contains a word
	 * @param word
	 * @return true if yes, false if no
	 */
	public boolean contains(String word) {
		if(list.contains(word))
			return true;
		else
			return false;
	}
	
	/**
	 * remove stop words from tokens
	 * @param tokens
	 * @return
	 */
	public String[] removeStopWordsFromTokens(String[] tokens) {
		ArrayList<String> internalList = new ArrayList<String>();
		for(String s : tokens) {
			if(!this.list.contains(s)) {
				internalList.add(s);
			}
		}
		return internalList.toArray(new String[internalList.size()]);
	}
	
	public boolean containsStopWord(String word) {
		if(this.list.contains(word))
			return true;
		else
			return false;
		
	}
	
	private void buildDefaultEnglishList() {
		list.add("a");
		list.add("about");
		list.add("above");
		list.add("after");
		list.add("again");
		list.add("against");
		list.add("all");
		list.add("am");
		list.add("an");
		list.add("and");
		list.add("any");
		list.add("are");
		list.add("aren't");
		list.add("as");
		list.add("at");
		list.add("be");
		list.add("because");
		list.add("been");
		list.add("before");
		list.add("being");
		list.add("below");
		list.add("between");
		list.add("both");
		list.add("but");
		list.add("by");
		list.add("can't");
		list.add("cannot");
		list.add("could");
		list.add("couldn't");
		list.add("did");
		list.add("didn't");
		list.add("do");
		list.add("does");
		list.add("doesn't");
		list.add("doing");
		list.add("don't");
		list.add("down");
		list.add("during");
		list.add("each");
		list.add("few");
		list.add("for");
		list.add("from");
		list.add("further");
		list.add("had");
		list.add("hadn't");
		list.add("has");
		list.add("hasn't");
		list.add("have");
		list.add("haven't");
		list.add("having");
		list.add("he");
		list.add("he'd");
		list.add("he'll");
		list.add("he's");
		list.add("her");
		list.add("here");
		list.add("here's");
		list.add("hers");
		list.add("herself");
		list.add("him");
		list.add("himself");
		list.add("his");
		list.add("how");
		list.add("how's");
		list.add("i");
		list.add("i'd");
		list.add("i'll");
		list.add("i'm");
		list.add("i've");
		list.add("if");
		list.add("in");
		list.add("into");
		list.add("is");
		list.add("isn't");
		list.add("it");
		list.add("it's");
		list.add("its");
		list.add("itself");
		list.add("let's");
		list.add("me");
		list.add("more");
		list.add("most");
		list.add("mustn't");
		list.add("my");
		list.add("myself");
		list.add("no");
		list.add("nor");
		list.add("not");
		list.add("of");
		list.add("off");
		list.add("on");
		list.add("once");
		list.add("only");
		list.add("or");
		list.add("other");
		list.add("ought");
		list.add("our");
		list.add("ours");
		list.add("ourselves");
		list.add("out");
		list.add("over");
		list.add("own");
		list.add("same");
		list.add("shan't");
		list.add("she");
		list.add("she'd");
		list.add("she'll");
		list.add("she's");
		list.add("should");
		list.add("shouldn't");
		list.add("so");
		list.add("some");
		list.add("such");
		list.add("than");
		list.add("that");
		list.add("that's");
		list.add("the");
		list.add("their");
		list.add("theirs");
		list.add("them");
		list.add("themselves");
		list.add("then");
		list.add("there");
		list.add("there's");
		list.add("these");
		list.add("they");
		list.add("they'd");
		list.add("they'll");
		list.add("they're");
		list.add("they've");
		list.add("this");
		list.add("those");
		list.add("through");
		list.add("to");
		list.add("too");
		list.add("under");
		list.add("until");
		list.add("up");
		list.add("very");
		list.add("was");
		list.add("wasn't");
		list.add("we");
		list.add("we'd");
		list.add("we'll");
		list.add("we're");
		list.add("we've");
		list.add("were");
		list.add("weren't");
		list.add("what");
		list.add("what's");
		list.add("when");
		list.add("when's");
		list.add("where");
		list.add("where's");
		list.add("which");
		list.add("while");
		list.add("who");
		list.add("who's");
		list.add("whom");
		list.add("why");
		list.add("why's");
		list.add("with");
		list.add("won't");
		list.add("would");
		list.add("wouldn't");
		list.add("you");
		list.add("you'd");
		list.add("you'll");
		list.add("you're");
		list.add("you've");
		list.add("your");
		list.add("yours");
		list.add("yourself");
		list.add("yourselves");
	}
}
