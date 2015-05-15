package open.sesame;

import java.util.HashSet;
import java.util.Set;

public class Token implements Comparable<Token>{
	public String id;
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
	//representative should be something like lemma:;:pos
	public Token(String representative) {
		id = representative;
	}
	@Override
	public String toString() {
		return id;
	}
	@Override
	public int compareTo(Token o) {
		if(this.tfidf > o.tfidf)
			return -1;
		else if (this.tfidf == o.tfidf)
			return 0;
		else
			return 1;
	}
}
