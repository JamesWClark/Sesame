package open.sesame;

import java.util.ArrayList;

import open.sesame.json.buckets.JsonToken;

public class ReviewWithTokens extends Review {
	public ArrayList<JsonToken> tokens;
	public ReviewWithTokens(String review_id) {
		super(review_id);
		tokens = new ArrayList();
		// TODO Auto-generated constructor stub
	}
	public ArrayList<JsonToken> getTokens() {
		return tokens;
	}
	public String getReviewId() {
		return this.id;
	}
}
