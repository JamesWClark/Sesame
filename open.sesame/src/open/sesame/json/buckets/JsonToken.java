package open.sesame.json.buckets;

public class JsonToken {
	public String id;
	public double score;
	public JsonToken(String id, double score) {
		this.id = id;
		this.score = score;
	}
	
	public double getTfidf() {
		return score;
	}
	
	public String getId() {
		return id;
	}
	
}
