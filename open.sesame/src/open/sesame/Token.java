package open.sesame;

import java.util.HashSet;
import java.util.Set;

public class Token {
	public String representative;
	public String word;
	public String lemma;
	public String pos;
	public int count = 0;
	public double tf;
	public double idf;
	public double tfidf;
	public Set<String> documents = new HashSet<String>();;
	public Token() {
		count++;
	}
	public Token(String rep) {
		count++;
		representative = rep;
	}
	@Override
	public String toString() {
		return representative;
	}
}
