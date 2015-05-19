package open.sesame;

public class Args {
	
	public static boolean auto = false;
	public static boolean tfidf = false;
	public static boolean lda = false;
	public static boolean countCategories = false;
	public static boolean singleCategory = false;
	public static boolean positivity = false;
	public static boolean negativity = false;
	public static int K = -1;
	public static double tmin = 1000.0;
	public static double tmax = -1000.0;
	public static String category = ""; //yelp, corenlp, inclusive, exclusive
	public static String sentimentCollection = "";
	
	/** THE BELOW COMMENTS NEVER GOT IMPLEMENTED **/
	
	// positive: sentimentCollection
	// yelp = stars > 3
	// corenlp = sa > neutral
	// inclusive = stars > 3 && sa > neutral
	// exclusive = stars > 3 || sa > neutral

	// negative: sentimentCollection
	// yelp = stars < 3
	// corenlp = sa < neutral
	// inclusive = stars < 3 && sa < neutral
	// exclusive = stars < 3 || sa < neutral	
	
	public static void setArgs(String[] args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i].toLowerCase().equals("-positivity")) { //goes with tfidf
				positivity = true;
				//sentimentCollection = args[i + 1];
			}
			if(args[i].toLowerCase().equals("-negativity")) { //goes with tfidf
				negativity = true;
				//sentimentCollection = args[i + 1];
			}
			if(args[i].equals("-auto")) {
				auto = true;
			}
			if(args[i].equals("countCategories")) {
				countCategories = true;
			}
			if(args[i].toLowerCase().equals("tfidf")) {
				tfidf = true;
			}
			if(args[i].toLowerCase().equals("lda")) {
				lda = true;
			}
			if(args[i].toLowerCase().equals("-k")) {
				K = Integer.parseInt(args[i + 1]);
			}
			if(args[i].toLowerCase().equals("-t")) {
				tmin = Double.parseDouble(args[i + 1]);
			}
			if(args[i].toLowerCase().equals("-tmin")) {
				tmin = Double.parseDouble(args[i + 1]);
			}
			if(args[i].toLowerCase().equals("-tmax")) {
				tmax = Double.parseDouble(args[i + 1]);
			}
			if(args[i].toLowerCase().equals("-category")) {
				singleCategory = true;
				category = args[i + 1];
			}
		}
		
		if(args.length == 0) {
			System.out.println("exception: args.length = 0");
			throw new UnsupportedOperationException();
		}
		if(tfidf == true && lda == true) {
			System.out.println("exception: tfidf == true && lda == true");
			throw new UnsupportedOperationException();
		}
		if(lda == true && (tmin == 1000.0 || K < 1)) {
			System.out.println("lda == true && (tmin == -1000.0 || K < 1)");
			throw new UnsupportedOperationException();
		}
	}
	public static void printInstructions() {
		System.out.println("USAGE\n======");
		System.out.println("tfidf -category \"Accessories\"");
		System.out.println("lda -k 10 -threshold 1.5 -category \"Accessories\"");
		System.out.println("lda -auto -tmin .5 -tmax .95 -category \"Accessories\"");
		System.out.println("-----");
		System.out.println("** -category is optional");
	}
}
