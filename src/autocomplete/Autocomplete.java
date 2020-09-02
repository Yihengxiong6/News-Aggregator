package autocomplete;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Autocomplete implements IAutocomplete {

	private Node root;
	private int numSuggestion;
	
	public Autocomplete() {
		this.root = new Node();
	}
	
	/**
	 * @return the root
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Node root) {
		this.root = root;
	}

	/**
	 * @return the numSuggestion
	 */
	public int getNumSuggestion() {
		return numSuggestion;
	}

	/**
	 * @param numSuggestion the numSuggestion to set
	 */
	public void setNumSuggestion(int numSuggestion) {
		this.numSuggestion = numSuggestion;
	}
	
	/**
     * Adds a new word with its associated weight to the Trie
     * 
     * @param word the word to be added to the Trie
     * @param weight the weight of the word
     */
	@Override
	public void addWord(String word, long weight) {
		
		// Check the illegal input
		if ((word == null)||(word.equals(""))|| (!word.matches("[a-zA-Z]+"))){ 
			return;
		}
		
		Node temp = root;
		word = word.toLowerCase();//Convert to lowercase.
		String wordTemp = new String(word);	//Create a copy for cut
		
		// DFS to add the word, stop when wordTemp is empty
		while(wordTemp.length()!= 0) { 
			temp.setPrefixes(temp.getPrefixes()+1);
			int index = wordTemp.charAt(0) - 'a'; // Get the index of the char in the references
			Node[] children = temp.getReferences();
			if(children[index] == null) { //If there is no such Node yet, create it.
				children[index] = new Node();
			}
			wordTemp = wordTemp.substring(1); // cut off the first char
			temp = children[index];
		}
		
		// after reach the last char of the word, update the related atrributes
		temp.setWords(temp.getWords()+1);
		temp.setPrefixes(temp.getPrefixes()+1);
		temp.setTerm(new Term(word, weight));		
	}


	/**
     * Initializes the Trie
     *
     * @param filename the file to read all the autocomplete data from each line
     *                 contains a word and its weight This method will call the
     *                 addWord method
     * @param k the maximum number of suggestions that should be displayed 
     * @return the root of the Trie You might find the readLine() method in
     *         BufferedReader useful in this situation as it will allow you to
     *         read a file one line at a time.
     */
	@Override
	public Node buildTrie(String filename, int k) {
		
		FileReader fr;
		BufferedReader br;
		this.numSuggestion = k;
		
		try {
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			String line = br.readLine(); // Ignore the first line

			// read each line until the line is null
			while((line = br.readLine())!= null) {
				line = line.trim();
				String[] wordWeight = line.split("\\t|\\s"); 
				
				// Check if the format is right
				if (wordWeight.length != 2) {
					continue;
				}
				
				this.addWord(wordWeight[1], Long.parseLong(wordWeight[0]));
			}	
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return root;
	}

	/**
     * @return k the the maximum number of suggestions that should be displayed 
     */
	@Override
	public int numberSuggestions() {
		return this.numSuggestion;
	}

    /**
     * @param prefix
     * @return the root of the subTrie corresponding to the last character of
     *         the prefix.
     */
	@Override
	public Node getSubTrie(String prefix) {
		
		Node temp = root;
		
		try {// Try and catch invalid value
		prefix = prefix.toLowerCase(); //convert string to lowercase
		
		// DFS to iterate the trie until prefix turns to a empty string
		while(prefix.length()!= 0) {
			int index = prefix.charAt(0) - 'a';
			Node[] children = temp.getReferences();
			
			 // if the reference is null
			if(children[index] == null) {
				System.out.println("No such prefix");
				return null;
			}
			
			prefix = prefix.substring(1);
			temp = children[index];
		}	
		}catch(Exception e){ 
			System.out.println("Illegal input");
			return null; // return null			
		}
		return temp;
	}

	 /**
     * @param prefix
     * @return the number of words that start with prefix.
     */
	@Override
	public int countPrefixes(String prefix) {
		
		if(prefix == null) { //If the prefix is null, return 0
			return 0;
		}
		
		if(this.getSubTrie(prefix) == null) { // If subtrie is null, return 0
			return 0;
		}	
		
		return this.getSubTrie(prefix).getPrefixes();
	}


    /**
     * This method should not throw an exception
     * @param prefix
     * @return a List containing all the ITerm objects with query starting with
     *         prefix. Return an empty list if there are no ITerm object starting
     *         with prefix.
     */
	@Override
	public List<ITerm> getSuggestions(String prefix) {
		
		ArrayList<ITerm> suggestions = new ArrayList<ITerm>();
		Node subRoot = this.getSubTrie(prefix);// get the subroot;		
		
		//If subroot is null
		if (subRoot == null){
			return suggestions; //return a empty string
		}	
		
		// Call the DFS recursive helper funtion
		getSuggestionsHelper(subRoot,suggestions); 
		Collections.sort(suggestions, ITerm.byPrefixOrder(Integer.MAX_VALUE));
		return suggestions;
	}
	
	/**
	 * Helper function of getSuggestions
	 * DFS the tree and find the words recursively
	 * @param vertex
	 * @param suggestions
	 */
	private void getSuggestionsHelper(Node vertex, ArrayList<ITerm> suggestions) {
		
		if(vertex == null) { // illegal input
			return;
		}
		
		// if there is word, add it to the suggestions list
		if(vertex.getWords() != 0) {
			suggestions.add((ITerm)vertex.getTerm());
		}
		
		// if the prefix is 0, it means that there is no word in its offspring, we stop
		if (vertex.getPrefixes() == 0) {
			return;
		}
		
		// Recursively update the list
		for(Node child: vertex.getReferences()) {
			getSuggestionsHelper(child, suggestions);
		}
	}
}
