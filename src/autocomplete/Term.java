package autocomplete;
public class Term implements ITerm {

	private String query;
	private long weight;
	
	// Constructor
	public Term(String query, long weight) {
		
		if((query == null)||(weight < 0)){ // If query is null or if weight is negative, throw an exception
			throw new IllegalArgumentException("Illegal arguments");
		}
		this.query = query;
		this.weight = weight;
	}	
	
	/**
	 * getter of the String of the item
	 * @return query String
	 */
	public String getTerm() {
		return this.query;
	}	
	
	/**
	 * setter of query
	 * @param query
	 */
	public void setTerm(String query) {
		this.query = query;
	}
	
	/**
	 * getter of weight
	 * @return weight
	 */
	public long getWeight() {
		return weight;
	}


	/**
	 * setter of weight
	 * @param weight
	 */
	public void setWeight(long weight) {
		this.weight = weight;
	}
	
	
	 // Compares the two terms in lexicographic order by query.
	@Override
	public int compareTo(ITerm that) {
		String w1 = ((Term)this).getTerm();
		String w2 = ((Term)that).getTerm();
       return w1.compareTo(w2);

	}	
	
	// Returns a string representation of this term in the following format:
    // the weight, followed by a tab, followed by the query.
	@Override
    public String toString() {
		String str = this.weight+"	"+query;
		return str;
    	
    }
}
