package open.sesame;

import java.util.ArrayList;

public class Review {
	public String id;
	public ArrayList<String> tokenIds = new ArrayList<String>(); 
	
	public Review(String review_id) {
		id = review_id;
	}
	
}
