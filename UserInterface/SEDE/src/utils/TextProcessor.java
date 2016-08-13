package utils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import data.Tweet;
import data.Word;
import database.Database;

public class TextProcessor {

	public static List<Word> gettopwords(List<Tweet> tweetdat) {
		Map<String, Integer> wordmap = new HashMap<String, Integer>();
		List<Word> wordlist = new ArrayList<Word>();
		Set<String> commonwordset=Database.getCommonWords();
		int commonwordscount=0;
		try {
			for (Tweet tobj : tweetdat) {
				List<String> bagofwords=gettokens(tobj.getBody());
				for (String word:bagofwords) {
					Integer count = wordmap.get(word);
					if (count == null) {
						count = new Integer(1);
					} else {
						count = new Integer(count.intValue() + 1);
					}
					wordmap.put(word, count);
				}
			}
			Map<String, Integer> sortedmap = sortByComparator(wordmap);
			int i = 0;
			for (String word : sortedmap.keySet()) {
				if (i == 20) {
					if(commonwordscount==0){
						break;
					}
					else{
						if(!commonwordset.contains(word)){
							Word words = new Word(word, sortedmap.get(word));
							wordlist.add(words);
							commonwordscount--;
						}
						continue;
					}
				}
				Word words = new Word(word, sortedmap.get(word));
				if(commonwordset.contains(word)){
					commonwordscount++;
					words.setCommon(true);
				}
				wordlist.add(words);
				i++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return wordlist;
	}

	private static Map<String, Integer> sortByComparator(
			Map<String, Integer> wordmap) {

		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				wordmap.entrySet());
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});
		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void filterdicttweets(List<Tweet> tweetlist, int categid) {
		Iterator<Tweet> tweetiterator = tweetlist.iterator();
		String pattern = Database.getpattern(categid);
		Pattern requiredpattern = Pattern.compile(pattern);
		while (tweetiterator.hasNext()) {
			Tweet currtweet = tweetiterator.next();
			//use complete string matching rather than tokenization
			boolean indict = requiredpattern.matcher(currtweet.getBody().toLowerCase()).matches();
			if (!indict) {
				tweetiterator.remove();
			}
		}
	}

	private static List<String> gettokens(String body) {
		List<String> wordlist = new ArrayList<String>();
		try {
			Analyzer analyzer = new StandardAnalyzer();
			TokenStream stream = analyzer.tokenStream(null, new StringReader(
					body));
			CharTermAttribute cattr = stream
					.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				String word = cattr.toString();
				wordlist.add(word);
			}
			stream.end();
			stream.close();
			analyzer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return wordlist;
	}
}
