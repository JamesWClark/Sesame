package open.sesame.nlp;

import java.util.HashMap;

public class PennTreebankPOS {
	
	private HashMap<String, String> map;
	
	
	//default
	public PennTreebankPOS() {
		map = new HashMap<String, String>();
		map.put("CC", "Coordinating conjunction");
		map.put("CD", "Cardinal number");
		map.put("DT", "Determiner");
		map.put("EX", "Existential there");
		map.put("FW", "Foreign word");
		map.put("IN", "Preposition or subordinating conjunction");
		map.put("JJ", "Adjective");
		map.put("JJR", "Adjective, comparative");
		map.put("JJS", "Adjective, superlative");
		map.put("LS", "List item marker");
		map.put("MD", "Modal");
		map.put("NN", "Noun, singular or mass");
		map.put("NNS", "Noun, plural");
		map.put("NNP", "Proper noun, singular");
		map.put("NNPS", "Proper noun, plural");
		map.put("PDT", "Predeterminer");
		map.put("POS", "Possessive ending");
		map.put("PRP", "Personal pronoun");
		map.put("PRP$", "Possessive pronoun");
		map.put("RB", "Adverb");
		map.put("RBR", "Adverb, comparative");
		map.put("RBS", "Adverb, superlative");
		map.put("RP", "Particle");
		map.put("SYM", "Symbol");
		map.put("TO", "to");
		map.put("UH", "Interjection");
		map.put("VB", "Verb, base form");
		map.put("VBD", "Verb, past tense");
		map.put("VBG", "Verb, gerund or present participle");
		map.put("VBN", "Verb, past participle");
		map.put("VBP", "Verb, non-3rd person singular present");
		map.put("VBZ", "Verb, 3rd person singular present");
		map.put("WDT", "Wh-determiner");
		map.put("WP", "Wh-pronoun");
		map.put("WP$", "Possessive wh-pronoun");
		map.put("WRB", "Wh-adverb");
	}
	//default
	public PennTreebankPOS(PennTreebankPOSOptions option) {
		switch(option) {
			case JWIPOS:
				map = new HashMap<String, String>();
				map.put("CC", "Coordinating conjunction");
				map.put("CD", "Cardinal number");
				map.put("DT", "Determiner");
				map.put("EX", "Existential there");
				map.put("FW", "Foreign word");
				map.put("IN", "Preposition or subordinating conjunction");
				map.put("JJ", "Adjective");
				map.put("JJR", "Adjective, comparative");
				map.put("JJS", "Adjective, superlative");
				map.put("LS", "List item marker");
				map.put("MD", "Modal");
				map.put("NN", "Noun, singular or mass");
				map.put("NNS", "Noun, plural");
				map.put("NNP", "Proper noun, singular");
				map.put("NNPS", "Proper noun, plural");
				map.put("PDT", "Predeterminer");
				map.put("POS", "Possessive ending");
				map.put("PRP", "Personal pronoun");
				map.put("PRP$", "Possessive pronoun");
				map.put("RB", "Adverb");
				map.put("RBR", "Adverb, comparative");
				map.put("RBS", "Adverb, superlative");
				map.put("RP", "Particle");
				map.put("SYM", "Symbol");
				map.put("TO", "to");
				map.put("UH", "Interjection");
				map.put("VB", "Verb, base form");
				map.put("VBD", "Verb, past tense");
				map.put("VBG", "Verb, gerund or present participle");
				map.put("VBN", "Verb, past participle");
				map.put("VBP", "Verb, non-3rd person singular present");
				map.put("VBZ", "Verb, 3rd person singular present");
				map.put("WDT", "Wh-determiner");
				map.put("WP", "Wh-pronoun");
				map.put("WP$", "Possessive wh-pronoun");
				map.put("WRB", "Wh-adverb");
				break;
		}
	}
	
	public String get(String key) {
		return map.get(key);
	}
}
