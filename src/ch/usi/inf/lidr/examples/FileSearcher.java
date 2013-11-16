/*
 * Copyright (C) 2013  Ilya Markov
 * 
 * Full copyright notice can be found in LICENSE. 
 */
package ch.usi.inf.lidr.examples;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ch.usi.inf.lidr.utils.ScoredEntity;

/**
 * Simulates retrieval using a TREC-formatted result file. 
 * 
 * @author Ilya Markov
 */
public final class FileSearcher {
	/**
	 * A mapping between queries and corresponding retrieval results.
	 */
	private final Map<String, List<ScoredEntity<String>>> query2results = new HashMap<String, List<ScoredEntity<String>>>(); 

	/**
	 * Constructs a searcher using a given file with TREC-formatted results.
	 * Considers <code>topN</code> documents for each query.
	 * 
	 * @param resultsFile The file with TREC-formatted results.
	 * @param topN The top-N, i.e. the number of documents to consider for each query.
	 * 		Should be positive.
	 * 
	 * @throws NullPointerException
	 * 		if <code>resultsFile</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 * 		if <code>resultsFile</code> is not a valid file.
	 * @throws IllegalArgumentException
	 * 		if <code>topN</code> is not positive.
	 */
	public FileSearcher(File resultsFile, int topN) {
		if (resultsFile == null) {
			throw new NullPointerException("The file with TREC-formatted results is null.");
		}
		if (!resultsFile.isFile()) {
			throw new IllegalArgumentException("Not a regular file: " + resultsFile.getAbsolutePath());
		}
		if (topN <= 0) {
			throw new IllegalArgumentException("The top-N is not positive: " + topN);
		}
		
		try {
			BufferedReader reader = null;
			try {
				String baseQuery = "1";
				List<ScoredEntity<String>> retrievalResults = new ArrayList<ScoredEntity<String>>(topN);
				int counter = 0;

				reader = new BufferedReader(new FileReader(resultsFile));				
				while(reader.ready()) {
					String line = reader.readLine();
					StringTokenizer tokenizer = new StringTokenizer(line);
					
					String query = tokenizer.nextToken();
					// If switched to a new query,
					// store the results and reinitialize.
					if (!baseQuery.equals(query)) {
						retrievalResults = retrievalResults.subList(0, counter);
						query2results.put(baseQuery, retrievalResults);
							
						baseQuery = query;
						retrievalResults = new ArrayList<ScoredEntity<String>>(topN);
						counter = 0;
					}
					
					// If not yet enough results are retrieved,
					// retrieve next document.
					if (retrievalResults.size() < topN) {
						// Skip one token
						tokenizer.nextToken();
						String doc = tokenizer.nextToken();
						// Skip one token
						tokenizer.nextToken();
						double score = Double.parseDouble(tokenizer.nextToken());
						retrievalResults.add(new ScoredEntity<String>(doc, score));
						counter++;
					}
				}
				
				query2results.put(baseQuery, retrievalResults);
			} finally {
				if (reader != null) reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Simulates retrieval.
	 * Returns retrieval results for a given <code>queryId</code>.
	 * 
	 * @param queryId The query id.
	 * 
	 * @return The retrieval results for a given <code>queryId</code>.
	 * 
	 * @throws NullPointerException
	 * 		if <code>queryId</code> is <code>null</code>.
	 */
	public List<ScoredEntity<String>> search(String queryId) {
		if (queryId == null) {
			throw new NullPointerException("The query id is null.");
		}
		
		if (query2results.containsKey(queryId)) {
			return query2results.get(queryId);
		}
		
		return new ArrayList<ScoredEntity<String>>();
	}
	
	/**
	 * Simulates retrieval.
	 * Returns the top N documents for a given <code>queryId</code>.
	 * 
	 * @param queryId The query id.
	 * @param topN The top-N. Should be positive.
	 * 
	 * @return The top N documents for a given <code>queryId</code>.
	 * 
	 * @throws NullPointerException
	 * 		if <code>queryId</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 * 		if <code>topN</code> is not positive.
	 */
	public List<ScoredEntity<String>> search(String queryId, int topN) {
		if (topN <= 0) {
			throw new IllegalArgumentException("The top-N is not positive: " + topN);
		}
		
		List<ScoredEntity<String>> result = search(queryId);
		result =result.subList(0, Math.min(topN, result.size()));
		return result;
	}
}
