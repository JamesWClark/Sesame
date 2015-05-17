package open.sesame;

public class Args {

	public static boolean tfidf = false;
	public static boolean lda = false;
	public static boolean countCategories = false;
	public static int K = -1;
	public static double threshold = -1000.0;
	
	public static void setArgs(String[] args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("countCategories")) {
				countCategories = true;
			}
			if(args[i].equals("tfidf")) {
				tfidf = true;
			}
			if(args[i].equals("lda")) {
				lda = true;
			}
			if(args[i].toLowerCase().equals("-k")) {
				K = Integer.parseInt(args[i + 1]);
			}
			if(args[i].toLowerCase().equals("-threshold")) {
				threshold = Double.parseDouble(args[i + 1]);
			}
		}
		
		if(args.length == 0) {
			throw new UnsupportedOperationException();
		}
		if(tfidf == true && lda == true) {
			throw new UnsupportedOperationException();
		}
		if(lda == true && (threshold == -1000.0 || K == -1 || K < 1)) {
			throw new UnsupportedOperationException();
		}
	}
	public static void printInstructions() {
		System.out.println("USAGE\n======");
		System.out.println("lda -k 10 -threshold 1.5");
		System.out.println("tfidf");
	}
}
