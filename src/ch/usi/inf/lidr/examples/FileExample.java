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
import java.util.Random;
import java.util.StringTokenizer;

import ch.usi.inf.lidr.merging.*;
import ch.usi.inf.lidr.norm.*;
import ch.usi.inf.lidr.selection.*;
import ch.usi.inf.lidr.utils.*;


/**
 * An example of LiDR usage,
 * when CSI and source-specific retrieval results
 * are stored in TREC-formatted files.
 *  
 * @author Ilya Markov
 */
public final class FileExample {

	/**
	 * The main executable method.
	 * 
	 * @param args The external parameters of the method. 
	 */
	public static void main(String[] args) {
		// Check parameters
		if (args.length < 1) {
			printUsage();
			System.exit(1);
		}
		String csiResultsPath = args[0];
		File csiResultsFile = new File(csiResultsPath);
		if (!csiResultsFile.exists() || !csiResultsFile.isFile()) {
			System.out.println("The CSI results file does not exist or is not a regular file: " + csiResultsPath);
			printUsage();
			System.exit(1);
		}
		String sourceSpecificResultsPath = args[1];
		File sourceSpecificResultsDir = new File(sourceSpecificResultsPath);
		if (!sourceSpecificResultsDir.exists() || !sourceSpecificResultsDir.isDirectory()) {
			System.out.println("The folder with source-specific results files does not exist or is not a regular directory: " + sourceSpecificResultsPath);
			printUsage();
			System.exit(1);
		}
		String doc2resourcePath = args[2];
		File doc2resourceFile = new File(doc2resourcePath);
		if (!doc2resourceFile.exists() || !doc2resourceFile.isFile()) {
			System.out.println("The document-to-resource mapping file does not exist or is not a regular file: " + doc2resourcePath);
			printUsage();
			System.exit(1);
		}

		// Initialize resource selection
		ResourceSelection selection = new ReDDETop();
		int kParam = 1000;
		((AbstractResourceSelection) selection).setCompleteRankCutoff(kParam);
		
		// Initialize score normalization
		ScoreNormalization normalization = new CORI();
		ScoreNormalization baseNormalization = new MinMax();
		((CORI) normalization).setNormalization(baseNormalization);
		double lambdaParam = 0.4;
		((CORI) normalization).setLambda(lambdaParam);
		
		// Initialize a CSI searcher
		int csiTopN = 1000;
		FileSearcher csiSearcher = new FileSearcher(csiResultsFile, csiTopN);
		
		List<Resource> resources = getResources();
		Map<Resource, FileSearcher> resourceSearchers = getResourceSearchers(resources, sourceSpecificResultsPath);
		Map<String, Resource> doc2resource = getDoc2Resource(doc2resourceFile, resources);
		
		// Create and run the example
		FileExample fileExample = new FileExample(selection, normalization);
		fileExample.run(csiSearcher, resourceSearchers, doc2resource);
	}

	/**
	 * Outputs the instructions.
	 */
	private static void printUsage() {
		System.out.println("USAGE: FileExample <csi results> <source-specific results> <document-to-resource mapping>");
		System.out.println("\tCSI results - a path to a TREC-formatted CSI results file.");
		System.out.println("\tSource-specific results - a path to a folder with TREC-formatted source-specific results files.");
		System.out.println("\tDocument-to-resource mapping - a file with the mapping between CSI documents and their corresponding resources.");
	}
	
	/**
	 * Returns a list of resources
	 * (assumes that there are 4 resources with ids from 1 to 4).
	 * The resource and sample sizes are generated randomly (for example purpose only!).
	 */
	private static List<Resource> getResources() {
		List<Resource> resources = new ArrayList<Resource>(4);
		Random random = new Random(42);
		for (int i = 1; i <= 4; i++) {
			String resourceId = Integer.toString(i);
			int fullSize = random.nextInt(100000);
			int sampleSize = random.nextInt(1000);
			Resource resource = new Resource(resourceId, fullSize, sampleSize);
			resources.add(resource);
		}
		return resources;
	}
	
	/**
	 * Returns a mapping between resources and their searchers.
	 * Assumes that for a given resource
	 * its TREC-formatted results file, located in <code>resultsDir</code>,
	 * has a name equal to resource id. 
	 */
	private static Map<Resource, FileSearcher> getResourceSearchers(List<Resource> resources,
			String resultsDir)
	{
		Map<Resource, FileSearcher> resourceSearchers = new HashMap<Resource, FileSearcher>();
		int resourceTopN = 100;
		for (Resource resource : resources) {
			File resourceResultsFile = new File(resultsDir + File.separator + resource.getResourceId());
			FileSearcher resourceSearcher = new FileSearcher(resourceResultsFile, resourceTopN);
			resourceSearchers.put(resource, resourceSearcher);
		}
		return resourceSearchers;
	}
	
	/**
	 * Returns a mapping between CSI documents and their corresponding resources.
	 */
	private static Map<String, Resource> getDoc2Resource(File doc2resourceFile,
			List<Resource> resources)
	{
		Map<String, Resource> doc2resource = new HashMap<String, Resource>();
		
		try {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(doc2resourceFile));
				while (reader.ready()) {
					String line = reader.readLine();
					StringTokenizer tokenizer = new StringTokenizer(line);
					
					String document = tokenizer.nextToken();
					String resourceId = tokenizer.nextToken();
					
					Resource resource = null;
					for (Resource res : resources) {
						if (resourceId.equals(res.getResourceId())) {
							resource = res;
							break;
						}
					}
					doc2resource.put(document, resource);
				}
			} finally {
				if (reader != null) reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return doc2resource;
	}
	
	
	/**
	 * The resource selection method.
	 */
	private final ResourceSelection selection;
	/**
	 * The score normalization method.
	 */
	private final ScoreNormalization normalization;
	
	/**
	 * Constructs the example.
	 * 
	 * @param selection The resource selection method.
	 * @param normalization The score normalization method.
	 * 
	 * @throws NullPointerException
	 * 		if <code>selection</code> or <code>normalization</code> is <code>null</code>.
	 */
	public FileExample(ResourceSelection selection, ScoreNormalization normalization) {
		if (selection == null) {
			throw new NullPointerException("The resource selection method is null.");
		}
		if (normalization == null) {
			throw new NullPointerException("The score normalization method is null.");
		}
		
		this.selection = selection;
		this.normalization = normalization;
	}

	/**
	 * Runs the example.
	 * 
	 * @param csiSearcher The CSI searcher.
	 * @param resourceSearchers The mapping between resources and their corresponding searchers.
	 * @param doc2resource The mapping between CSI documents and their corresponding resources.
	 * 
	 * @throws NullPointerException
	 * 		if <code>csiSearcher</code>, or <code>resourceSearchers</code>,
	 * 		or <code>doc2resource</code> is <code>null</code>.
	 */
	public void run(FileSearcher csiSearcher,
			Map<Resource, FileSearcher> resourceSearchers,
			Map<String, Resource> doc2resource)
	{
		if (csiSearcher == null) {
			throw new NullPointerException("The CSI searcher is null.");
		}
		if (doc2resource == null) {
			throw new NullPointerException("The mapping between documents and resources is null.");
		}
		
		// Process queries (assumes that there are 5 queries with ids from 1 to 5)
		for (int queryId = 1; queryId <= 5; queryId++) {
			System.out.println("Processing query " + queryId);
			List<ScoredEntity<String>> mergedResult = new ArrayList<ScoredEntity<String>>();
			
			// Obtain a CSI ranking of documents and a list of corresponding sources
			List<ScoredEntity<String>> csiDocs = csiSearcher.search(Integer.toString(queryId));
			List<Resource> resources = getResources(csiDocs, doc2resource);
//			System.out.println(csiDocs);
//			System.out.println(resources);
			
			// Run resource selection and normalize resource scores
			List<ScoredEntity<Resource>> scoredResources = selection.select(csiDocs, resources);
			List<ScoredEntity<Resource>> normResources = new MinMax().normalize(scoredResources);
			System.out.println("\tResource ranking: " + normResources);
			
			for (ScoredEntity<Resource> normResource : normResources) {
				// Retrieve source-specific results
				FileSearcher resourceSearcher = resourceSearchers.get(normResource.getEntity());
				List<ScoredEntity<String>> resourceDocs = resourceSearcher.search(Integer.toString(queryId));
				
				// Run normalization:
				// 1. Set the relevance of the result list.
				//    NOTE: Type conversion is not a very nice coding style
				//          and is performed for the purpose of the example only.
				((CORI) normalization).setResultListRelevance(normResource.getScore());
				// 2. Normalize document scores
				List<ScoredEntity<String>> normDocs = normalization.normalize(resourceDocs);
				// 3. Merge with already processed results
				mergedResult = MergeSort.sort(mergedResult, normDocs);
			}
			
			System.out.println("\tDocument ranking: " + mergedResult);
		}
	}
	
	/**
	 * For a given list of documents returns a list of their corresponding resources.
	 */
	private List<Resource> getResources(List<ScoredEntity<String>> documents,
			Map<String, Resource> doc2resource)
	{
		List<Resource> resources = new ArrayList<Resource>(documents.size());
		List<ScoredEntity<String>> filteredDocs = new ArrayList<ScoredEntity<String>>(documents.size());
		filteredDocs.addAll(documents);
		
		for (ScoredEntity<String> document : documents) {
			Resource resource = doc2resource.get(document.getEntity());
			if (resource == null) {
				filteredDocs.remove(document);
			} else {
				resources.add(resource);
			}
		}
		
		documents.clear();
		documents.addAll(filteredDocs);
		
		return resources;
	}
}
