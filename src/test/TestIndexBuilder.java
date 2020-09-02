package test;

import org.junit.Test;

import indexing.IndexBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.junit.Before;


/**
 * @author ericfouh
 */
public class TestIndexBuilder {
	
	/**
	 * RSS FEED:    http://cit594.ericfouh.com/sample_rss_feed.xml
	 * 
	 * http://cit594.ericfouh.com/page1.html: 
	 * data structures: linear data structures Lists: arraylist, linkedlist, stacks, queues
	 * 
	 * http://cit594.ericfouh.com/page2.html: 
	 * data structures: linear data structures Lists: arraylist, linkedlist, stacks, queues 
	 * Binary trees are very efficient at managing large collections. Trees can be used to compress files. 
	 * binary search trees are sorted data structures (total order) binary heaps are partially ordered 
	 * data structures Implementing an order on the data allows for faster storage, search and retrieval.
	 * 
	 * http://cit594.ericfouh.com/page3.html:
	 * when working with binary trees, you can implement a natural order (on the data) or pass a comparator object. 
	 * treeset and tree map in Java use red-black trees, a type of self-balancing trees.
	 * 
	 * http://cit594.ericfouh.com/page4.html:
	 * This file has nothing to do with the others. maybe I should paste a poem by Mallarme here. What do you think?
	 * 
	 * http://cit594.ericfouh.com/page5.html:
	 * Let's see how this categorization will work. three files talked about CIT594 topics and one is completely random.
	 */

	String html1;
	String html2;
	String html3;
	String html4;
	String html5;
	IndexBuilder tIndex;
	ArrayList<String> feeds;
	
	@Before
	public void setUp() throws Exception {
		html1 = "http://cit594.ericfouh.com/page1.html";
		html2 = "http://cit594.ericfouh.com/page2.html";
		html3 = "http://cit594.ericfouh.com/page3.html";
		html4 = "http://cit594.ericfouh.com/page4.html";
		html5 = "http://cit594.ericfouh.com/page5.html";
		
		feeds = new ArrayList<String>();
		feeds.add("http://cit594.ericfouh.com/sample_rss_feed.xml");
		tIndex = new IndexBuilder();
	}


//System.out.println()
	@Test
	public void testparseFeed() {
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		
		// Test correct number of files
		assertTrue(parsedFeed.size() == 5);
		
		// Test if the map contains the names of the documents (URLs/keys)
		assertTrue(parsedFeed.containsKey(html1));
		assertTrue(parsedFeed.containsKey(html2));
		assertTrue(parsedFeed.containsKey(html3));
		assertTrue(parsedFeed.containsKey(html4));
		assertTrue(parsedFeed.containsKey(html5));

		//Test if the map contains the correct number of terms in the lists (values)
		assertEquals(parsedFeed.get(html1).size(), 10);
		assertEquals(parsedFeed.get(html2).size(), 55);
		assertEquals(parsedFeed.get(html3).size(), 33);
		assertEquals(parsedFeed.get(html4).size(), 22);
		assertEquals(parsedFeed.get(html5).size(), 18);	
	}

	/**
	 * the key is the document, the value is a map of a tag term and its TFIDF value. 
	 */
	@Test
	public void testbuildIndex() { 
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		Map<String, Map<String, Double>> buildIndex = tIndex.buildIndex(parsedFeed);
		
		// Test TFIDF values
		assertEquals(buildIndex.get(html1).get("data"), 0.1021, 0.0001);
		assertEquals(buildIndex.get(html1).get("structures"), 0.183, 0.001);
		assertEquals(buildIndex.get(html2).get("data"), 0.046, 0.001);
		assertEquals(buildIndex.get(html5).get("categorization"), 0.0894, 0.0001);
		assertEquals(buildIndex.get(html3).get("binary"), 0.0277, 0.0001);
	}
	
	/**
	 * Build an inverted index consisting of a map of each tag term and a Collection (Java)  
	 * of Entry objects mapping a document with the TFIDF value of the term 
	 */
	@Test
	public void testbuildInvertedIndex(){ 
		
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		Map<String, Map<String, Double>> buildIndex = tIndex.buildIndex(parsedFeed);
		
		
		// Test if the map is of the correct type (of Map)		
		// Test if the associates the correct files to a term
		HashMap<String, TreeSet<Entry<String, Double>>> InvertedIndex = (HashMap<String, TreeSet<Entry<String, Double>>>) tIndex.buildInvertedIndex(buildIndex);
		
		assertEquals(InvertedIndex.get("data").size(), 3); // Test term "data" page1-3
		assertEquals(InvertedIndex.get("structures").size(), 2); // Test term "structures" page4
		assertEquals(InvertedIndex.get("completely").size(), 1); // Test term "completely" page5
		assertEquals(InvertedIndex.get("mallarme").size(), 1); // Test term "mallarme" page4
		
		// Test if the map stores the documents in the correct order 
		assertTrue(InvertedIndex.get("data").first().getKey().equals("http://cit594.ericfouh.com/page1.html"));
		assertTrue(InvertedIndex.get("structures").first().getKey().equals("http://cit594.ericfouh.com/page1.html"));				
	}
	
	/**
	 * a sorted collection of terms and articles Entries are sorted by number of articles.    
	 */
	@Test
	public void testbuildHomePage(){ 
		
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		Map<String, Map<String, Double>> buildIndex = tIndex.buildIndex(parsedFeed);
		HashMap<String, TreeSet<Entry<String, Double>>> InvertedIndex = (HashMap<String, TreeSet<Entry<String, Double>>>) tIndex.buildInvertedIndex(buildIndex);
		
		// Test if the collection is the correct type 
		// Test if collection stores the entries are in the correct order
		TreeSet<Entry<String, List<String>>> homePage = (TreeSet<Entry<String, List<String>>>) tIndex.buildHomePage(InvertedIndex);
		ArrayList<Entry<String, List<String>>> homePageList = new ArrayList<Entry<String, List<String>>>(homePage);
		assertEquals(homePageList.get(0).getKey(), "data");
		assertEquals(homePageList.get(1).getKey(), "trees");
		assertEquals(homePageList.get(2).getKey(), "structures");
		assertEquals(homePageList.get(3).getKey(), "stacks");	
	}
	
	/**
	 * The users should be able to enter a query term and our news aggregator will return all the articles related (tagged) to that term.  
	 */
	@Test
	public void testsearchArticles(){ 
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		Map<String, Map<String, Double>> buildIndex = tIndex.buildIndex(parsedFeed);
		HashMap<String, TreeSet<Entry<String, Double>>> invertedIndex = (HashMap<String, TreeSet<Entry<String, Double>>>) tIndex.buildInvertedIndex(buildIndex);
		
		
		// test if Your list contains the correct number of articles
		ArrayList<String> articles1 = (ArrayList<String>) tIndex.searchArticles("data", invertedIndex);
		ArrayList<String> articles2 = (ArrayList<String>) tIndex.searchArticles("think", invertedIndex);
		ArrayList<String> articles3 = (ArrayList<String>) tIndex.searchArticles("files", invertedIndex);
		assertEquals(articles1.size(), 3);
		assertEquals(articles2.size(), 1);
		assertEquals(articles3.size(), 2);
		
		// test if Your list contains the correct of articles
		
		// articles1
		assertTrue(articles1.contains(html1) && articles1.contains(html2) && articles1.contains(html3));
		assertTrue(articles2.contains(html4));
		assertTrue(articles3.contains(html5) && articles3.contains(html2));		
	}
	
	@Test
	public void testcreateAutocompleteFile(){ 
		Map<String, List<String>> parsedFeed = tIndex.parseFeed(feeds);
		Map<String, Map<String, Double>> buildIndex = tIndex.buildIndex(parsedFeed);
		HashMap<String, TreeSet<Entry<String, Double>>> invertedIndex = (HashMap<String, TreeSet<Entry<String, Double>>>) tIndex.buildInvertedIndex(buildIndex);
		
		// Test if your collection is of the correct type
		// Test if your collection contains the correct number of words
		TreeSet<String> words = (TreeSet<String>) tIndex.createAutocompleteFile(tIndex.buildHomePage(invertedIndex));
		assertTrue(words.size() == 57);
		
		
	}
}
