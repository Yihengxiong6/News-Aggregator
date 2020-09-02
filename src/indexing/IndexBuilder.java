package indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexBuilder implements IIndexBuilder {

	 /**
     * <parseFeed> Parse each document/rss feed in the list and return a Map of
     * each document and all the words in it. (punctuation and special
     * characters removed)
     * 
     * @param feeds a List of rss feeds to parse
     * @return a Map of each documents (identified by its url) and the list of
     *         words in it.
     */
	@Override
	public Map<String, List<String>> parseFeed(List<String> feeds) {
		Map<String, List<String>> parsedFeed = new HashMap<String, List<String>>();		
		
		try {
			for(String feed: feeds) {
				Document doc = Jsoup.connect(feed).get();
				Elements links = doc.getElementsByTag("link"); // all links from a RSS file
				
				
				for (Element link : links){ // extract each html link from a RSS file
					
					String linkText = link.text();// Initiate a key
					List<String> bodyStrings = new ArrayList<String>(); // Initate the value
					
					Document htmlDoc = Jsoup.connect(linkText).get(); 	
					Elements bodys = htmlDoc.getElementsByTag("body"); // The body elements		
					
					String textHTML = bodys.text().toLowerCase(); 
					String[] words = textHTML.replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+"); // delete the punctuations
					
					for(int i = 0; i < words.length; i++) {
						 bodyStrings.add(words[i]);
					}
									
					parsedFeed.put(linkText,  bodyStrings);
					
				}					
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}						
		return parsedFeed;
	}

	  /**
     * @param docs a map computed by {@parseFeed}
     * @return the forward index: a map of all documents and their 
     *         tags/keywords. the key is the document, the value is a 
     *         map of a tag term and its TFIDF value. 
     *         The values (Map<String, Double>) are sorted
     *         by lexicographic order on the key (tag term)
     *  
     */
	@Override
	public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
		// TF(t) = (Number of times term t appears in a document) 1/ (Total number of terms in the document 1) Within one doc
		// IDF(t) = log_e(Total number of documents/ Number of documents with term t in it). Among the different docs
		// TF-IDF(t) = TF * IDF
		
		Map<String, Map<String, Double>> indexMap = new HashMap<String, Map<String, Double>>(); // the output map						
		int totalDoc = docs.size(); // total amount of docs
		
		// First to get the data for calculatate IDF
		Map<String, Integer> exist = new HashMap<String, Integer>(); // Map to store number of documents that exist the term(Key)
		for(Entry<String, List<String>> htmlE: docs.entrySet()){ // To get the number of documents that exist each term
			Set<String> terms = new HashSet<String>(htmlE.getValue()); // First convert the String list of a html doc to a Set
			
			for(String term: terms) {
				if(!exist.containsKey(term)) {
					exist.put(term, 0);
				}
				exist.put(term, exist.get(term) + 1);
			}
		}				
		
		// Get the data for TF and calculate TF-IDF(t) = TF * IDF in the iteration of each html file.
		for(Entry<String, List<String>> htmlF: docs.entrySet()){
			int totalTerm = htmlF.getValue().size(); // the num of terms in each html files
			Map<String, Integer> numOfTerm = new HashMap<String, Integer>(); // initiate a map to store the number of each term in the html file
			Map<String, Double> TFIDF = new TreeMap<String, Double>();
			List<String> terms = htmlF.getValue();
			
			for(String term: terms) { // track the num of each term within a file
				if(!numOfTerm.containsKey(term)) {
					numOfTerm.put(term, 0);
				}
				numOfTerm.put(term, numOfTerm.get(term) + 1);
			} 
			
			// To store each term's TF-IDF(t) in the file. TreeMap is sorted on lexicographic order on the key by default.
			for(Entry<String, Integer> num: numOfTerm.entrySet()){
				String term = num.getKey();
				double data = ((double)num.getValue()/(double)totalTerm) * Math.log((double)totalDoc/(double)exist.get(term));
				TFIDF.put(term, data);				
			}
			
			// Store the data for each html file.
			indexMap.put(htmlF.getKey(), TFIDF);						
		}		
		return indexMap;
	}
	
	/**
	 * Create a comparator for the TreeSet in the task 4
	 * @return
	 */
	public static Comparator<Entry<String, Double>> indexComparator(){
        
        return new Comparator<Entry<String, Double>>() {
        	@Override
            public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2)
            {
            	//sort the entry by reverse tag term TFIDF value
            	return entry2.getValue().compareTo(entry1.getValue());         	
            }
        };
    }
    
    /**
     * Build an inverted index consisting of a map of each tag term and a Collection (Java)
     * of Entry objects mapping a document with the TFIDF value of the term 
     * (for that document)
     * The Java collection (value) is sorted by reverse tag term TFIDF value 
     * (the document in which a term has the
     * highest TFIDF should be listed first).
     * 
     * 
     * @param index the index computed by {@buildIndex}
     * @return inverted index - a sorted Map of the documents in which term is a keyword
     */
	@Override
	public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {
		
		Comparator<Entry<String, Double>> comp = IndexBuilder.indexComparator(); // Initiate a comparator for the treeSet
		Map<String, HashMap<String, Double>> invertedIndex = new HashMap<String, HashMap<String, Double>>(); // we cannot sort in hashmap, so just use it for store the entry
		Map<String, TreeSet<Entry<String, Double>>> invertedIndexSorted = new HashMap<String, TreeSet<Entry<String, Double>>>(); // This is the datatype I choose to return		
		
		// To visit the entryset of index Map
		for(Entry<String, Map<String, Double>> doc: index.entrySet()){ // Visit every doc, the key is the new key of the collection of entries
			String docName = doc.getKey();
			
			for(Entry<String, Double> termData: doc.getValue().entrySet()) { // Visit every term - data entry
				String term = termData.getKey(); // The term
				Double data = termData.getValue(); // The TF-IDF
				
				if (!invertedIndex.containsKey(term)) { // If the map does not contain the term 
					invertedIndex.put(term, new HashMap<String, Double>()); // The hashmap is for storing entry of doc and TFIDF
				}
				
				HashMap<String, Double> docEntry = invertedIndex.get(term); // get the hashmap of the related term
				docEntry.put(docName, data); // Put new entry of doc and data to the map
			}
		}
		
		// transfer the index from invertedIndex to invertedIndexSorted
		// the value if two map is different, in invertedIndexFinal, we use a treeSet to sort the index
		for(Entry<String, HashMap<String, Double>> termIndex: invertedIndex.entrySet()) {
			String term = termIndex.getKey(); // The term
			TreeSet<Entry<String, Double>> sortedIndex = new TreeSet<Entry<String, Double>>(comp); // Create treeset for every term
			
			// Add the entry of doc and data from hashmap to the treeset so we can sort the entry within the treeset.
			for(Entry<String, Double> termData: termIndex.getValue().entrySet()) { 
				sortedIndex.add(termData);
			}
			invertedIndexSorted.put(term, sortedIndex);
		}				
		
		return invertedIndexSorted;
	}

	/**
	 * Create a comparator for the TreeSet in the task 5
	 * Tag terms are sorted by the number of articles. 
	 * If two terms have the same number of articles, then they should be sorted by reverse lexicographic order.
	 * @return
	 */
	public static Comparator<Entry<String, List<String>>> homePageComparator(){
        
        return new Comparator<Entry<String, List<String>>>() {
        	@Override
            public int compare(Entry<String, List<String>> entry1, Entry<String, List<String>> entry2)
            {
            	if(entry1.getValue().size() != entry2.getValue().size()) { // Tag terms are sorted by the number of articles. 
            		return (entry2.getValue().size() - entry1.getValue().size());
            	}else { //If two terms have the same number of articles, then they should be sorted by reverse lexicographic order.
            		return entry2.getKey().compareTo(entry1.getKey()); 
            	}
            	       	
            }
        };
    }
	
    /**
     * @param invertedIndex
     * @return a sorted collection of terms and articles Entries are sorted by
     *         number of articles. If two terms have the same number of 
     *         articles, then they should be sorted by reverse lexicographic order.
     *         The Entry class is the Java abstract data type
     *         implementation of a tuple
     *         https://docs.oracle.com/javase/9/docs/api/java/util/Map.Entry.html
     *         One useful implementation class of Entry is
     *         AbstractMap.SimpleEntry
     *         https://docs.oracle.com/javase/9/docs/api/java/util/AbstractMap.SimpleEntry.html
     */
	@Override
	public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) { // Map<String, TreeSet<Entry<String, Double>>>
		
		Collection<Entry<String, List<String>>> homePage = new TreeSet<Entry<String, List<String>>>(homePageComparator()); // The final collection
		
		Map<String, TreeSet<Entry<String, Double>>> IndexMap = (Map<String, TreeSet<Entry<String, Double>>>) invertedIndex;
		for(Entry<String, TreeSet<Entry<String, Double>>> termData: IndexMap.entrySet()) {
			String term = termData.getKey();
			
			if(!STOPWORDS.contains(term)) { // If not contained in stopword, then added to the homepage
				ArrayList<String> articles = new ArrayList<String>();
				
				for(Entry<String, Double> data: termData.getValue()) { // Add articles to the list
					articles.add(data.getKey());
				}
				
				homePage.add(new AbstractMap.SimpleEntry<String,List<String>>(term, articles));
			}

		}
		
		return homePage;
	}

	// 
    /**
     * Create a file containing all the words in the inverted index. Each word
     * should occupy a line Words should be written in lexicographic order
     * assign a weight of 0 to each word. The method must store the words into a 
     * file named autocomplete.txt
     * 
     * @param homepage the collection used to generate the homepage (buildHomePage)
     * @return A collection containing all the words written into the file sorted by lexicographic order
     */
	@Override
	public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {
		
		Collection<String> words = new TreeSet<String>(
				new Comparator<String>() {

					@Override
					public int compare(String arg0, String arg1) {
						return arg0.compareTo(arg1);
					}
				});
		
		for(Entry<String, List<String>> term: homepage) {
			words.add(term.getKey());
		}
		
		FileWriter fw;
		BufferedWriter br;
		try {
			fw = new FileWriter("autocomplete.txt");
			br = new BufferedWriter(fw);
			br.write(words.size() + ""); // First write the size it the first line
			Iterator<String> itword = words.iterator();
			
			while(itword.hasNext()) {	
				br.newLine();
				br.write(" 0 " + itword.next());
			}
			br.flush();
			fw.close();

			
		}catch (IOException e) {
			
			e.printStackTrace();
		}

		
		return words;
	}

	/**
     * @param queryTerm
     * @param invertedIndex
     * @return
     */
	@Override
	public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {
		
		List<String> articles = new ArrayList<String>(); 
		TreeSet<Entry<String, Double>> articleSet = (TreeSet<Entry<String, Double>>) invertedIndex.get(queryTerm);
		if(!invertedIndex.containsKey(queryTerm)) { //If not contains such query Term, return null
			return null;
		}
		for(Entry<String, Double> termArticle: articleSet) {
			articles.add(termArticle.getKey());
		}		
		return articles; 
	}
}
