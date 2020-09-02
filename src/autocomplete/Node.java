package autocomplete;
/**
 * ==== Attributes ====
 * - words: number of words
 * - term: the ITerm object
 * - prefixes: number of prefixes 
 * - references: Array of references to next/children Nodes
 * 
 * ==== Constructor ====
 * Node(String word, long weight)
 * 
 * @author Your_Name
 */
public class Node
{
	private Term term;
	private int words;
	private int prefixes;
	private Node[] references;
	
	// Constructor 1, for word
	public Node(String query, long weight) {
		if((query == null)||(weight < 0)){ // If query is null or if weight is negative, throw an exception
			throw new IllegalArgumentException("Illegal arguments");
		}
		this.term = new Term(query, weight); // call the constructor of Term
		this.words = 0;
		this.prefixes = 0;
		this.references = new Node[26]; // initialize the 26 references slot.	
	}
	
	public Node() {
		this.words = 0;
		this.prefixes = 0;
		this.references = new Node[26]; // initialize the 26 references slot.	
	}
	
	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public int getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(int prefixes) {
		this.prefixes = prefixes;
	}

	public Node[] getReferences() {
		return references;
	}

	public void setReferences(Node[] reference) {
		this.references = reference;
	}
	
	
	public boolean isLeaf(){
		for(Node n: this.references) {
			if(n != null) {
				return false;
			}
		}
		return true;
	}
	
	@Override
    public String toString() {
		return (term + " words:" + this.getWords() + " prefixes:" + this.getPrefixes());
	}
}
