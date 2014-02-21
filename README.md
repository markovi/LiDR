LiDR - Library for Distributed Retrieval
====


## General Information
LiDR is an open-source java library available under the GNU General Public License. It provides methods for Distributed Information Retrieval (DIR). In particular, LiDR implements unsupervised small-document approaches to resource selection and a number of score normalization and results merging methods.

If you use this code, please refer to one of the following publications:
* [[bib](http://www.inf.usi.ch/phd/markov/publications/Markov_TOIS2014.bib)] Ilya Markov and Fabio Crestani. **Theoretical, Qualitative and Quantitative Analyses of Small-Document Approaches to Resource Selection.** To appear in the *ACM Transactions on Information Systems*, 2014.
* [[bib](http://www.inf.usi.ch/phd/markov/publications/Markov_PhDThesis2014.bib)] Ilya Markov. **Uncertainty in Distributed Information Retrieval.** PhD Thesis, University of Lugano, 2014.

The list of my other publications on DIR is available [here](http://www.inf.usi.ch/phd/markov/publications.html).

For any questions, bugs or feature requests please submit an [issue](https://github.com/markovi/LiDR/issues).


#### Distributed Information Retrieval
Distributed Information Retrieval (DIR) provides methods for aggregating multiple searcheable sources of information under a single interface. DIR usually consists of the following phases: resource description, resource selection, score normalization, results merging and results presentation. For more information on DIR please refer to the following publications and check out our [DIR tutorial](http://www.inf.usi.ch/phd/markov/tutorial.html):
* Fabio Crestani and Ilya Markov. **Distributed Information Retrieval and Applications.** In *Proceedings of ECIR*, pages 865-868, 2013.
* Milad Shokouhi and Luo Si. **Federated search.** *Foundations and Trends in Information Retrieval*, 5:1--102, 2011.
* Jamie Callan. **Distributed Information Retrieval.**
*Advances in Information Retrieval*, pages 127-150, Kluwer Academic Publishers, 2000.


#### Implemented Methods
LiDR implements the following resource selection techniques:
* **ReDDE**: Luo Si and Jamie Callan. **Relevant document distribution estimation method for resource selection.** In *Proceedings of SIGIR*, pages 298-305, 2003.
* **CRCS**: Milad Shokouhi. **Central-Rank-Based Collection Selection in Uncooperative Distributed Information Retrieval.** In *Proceedings of ECIR*, pages 160--172, 2007.
* **CiSS**: Georgios Paltoglou, Michail Salampasisl and Maria Satratzemi. **Integral based source selection for uncooperative distributed information retrieval environments.** In *Proceedings of LSDS-IR workshop*, pages 67-74, 2008.
* **GAVG**: Jangwon Seo and Bruce W. Croft. **Blog site search using resource selection.** In *Proceedings of CIKM*, pages 1053--1062, 2008.
* **SUSHI**: Paul Thomas and Milad Shokouhi. **SUSHI: scoring scaled samples for server selection.** In *Proceedings of SIGIR*, pages 419--426, 2009.
* **ReDDE.top**: Jaime Arguello, Jamie Callan and Fernando Diaz. **Classification-based resource selection.** In *Proceedings of CIKM*, pages 1277-1286, 2009.

LiDR also provides implementations of the following score normalization and results merging methods:
* **MinMax**: Joon Ho Lee. **Analyses of multiple evidence combination.** In *Proceedings of SIGIR*, pages 267-276, 1997.
* **Z-Score**, **Sum**: Mark Montague and Javed A. Aslam. **Relevance score normalization for metasearch.** In *Proceedings of CIKM*, pages 427-433, 2001.
* **CORI**: James P. Callan, Zhihong Lu and W. Bruce Croft. **Searching distributed collections with inference networks.** In *Proceedings of SIGIR*, pages 21-28, 1995.
* **SSL**: Luo Si and Jamie Callan. **A semisupervised learning method to merge search engine results.** *ACM Transactions on Information Systems*, pages 457-491, 2003.
* **SAFE**: Milad Shokouhi and Justin Zobel. **Robust result merging using sample-based score estimates.** *ACM Transactions on Information Systems*, 27:3, pages 1-29, 2009.


## Downloading and Installing
LiDR can be downloaded and built as shown bellow.

```bash
cd BASE_DIR
git clone git://github.com/markovi/LiDR.git
cd LiDR
ant jar
java -cp "build/jar/lidr-1.0-SANPSHOT.jar" ch.usi.inf.lidr.examples.FileExample data/csi_result data/source_results/ data/doc2resource
```

Here ```BASE_DIR``` is the base folder, to which the library should be downloaded. The script above enters the base folder (line 1) and downloads the library located at https://github.com/markovi/LiDR (line 2). Then it builds the library by entering the LiDR folder (line 3) and running the ```ant``` task (line 4). Line 5 runs the example provided with the code and discussed in more details [below](https://github.com/markovi/LiDR/edit/master/README.md#usage).



## How to Use LiDR
An example of LiDR usage can be found in the ```ch.usi.inf.lidr.examples.FileExample``` class. This example assumes that the retrieval results of a centralized sample index (CSI) and of each source are stored in corresponding files according to the TREC format. The mapping between documents in CSI and their sources is also stored in a file. The data for this example is located in the ```data``` folder: the ```csi_result``` file contains retrieval results of CSI for five queries, the ```source_results``` folder contains retrieval results of four different sources for the same queries and the ```doc2resource``` file contains the mapping between CSI documents and their sources. ```FileExample``` can be run as shown in the listing [above](https://github.com/markovi/LiDR/edit/master/README.md#downloading-and-installing). For each query it outputs the ranking of sources with source scores and the ranking of documents with normalized documents scores.

The listing below shows, how ```FileExample``` initializes resource selection and score normalization methods. In particular, this example uses ReDDE.top for resource selection and CORI for score normalization. Lines 2-4 create an instance of the ```ReDDETop``` class (line 2) and set the value of its parameter *k* (lines 3-4). Lines 7-11 create an instance of the ```CORI``` class (line 7), set its base score normalization method, in this case MinMax, (lines 8-9) and set the value of the parameter *lambda* (lines 10-11).

```java
// Initialize resource selection
ResourceSelection selection = new ReDDETop();
int k = 1000;
((AbstractResourceSelection) selection).setCompleteRankCutoff(k);

// Initialize score normalization
ScoreNormalization normalization = new CORI();
ScoreNormalization baseNormalization = new MinMax();
((CORI) normalization).setNormalization(baseNormalization);
double lambdaParam = 0.4;
((CORI) normalization).setLambda(lambdaParam);
```

The next listing shows how LiDR can be used for distributed retrieval. In lines 6-7 the CSI ranking and a list of corresponding sources are obtained. In line 10 resource selection is performed, while in line 11 source scores are normalized for later use with CORI score normalization. Lines 13-25 iterate over the selected sources. First, the source-specific document ranking is obtained for each source in line 16. Then the corresponding source score is passed to CORI in line 20 to weigh normalized document scores. Normalization is performed in line 22 and, finally, the normalized ranking is merged with results of other sources in line 24.

```java
//Process queries (assumes that there are 5 queries with ids from 1 to 5)
for (int queryId = 1; queryId <= 5; queryId++) {
	List<ScoredEntity<String>> mergedResult = new ArrayList<ScoredEntity<String>>();

	// Obtain a CSI ranking of documents and a list of corresponding sources
	List<ScoredEntity<String>> csiDocs=csiSearcher.search(Integer.toString(queryId));
	List<Resource> resources=getResources(csiDocs, doc2resource);

	// Run resource selection and normalize resource scores
	List<ScoredEntity<Resource>> scoredResources=selection.select(csiDocs,resources);
	List<ScoredEntity<Resource>> normResources=new MinMax().normalize(scoredResources);

	for (ScoredEntity<Resource> normResource : normResources) {
		// Retrieve source-specific results
		FileSearcher resourceSearcher = resourceSearchers.get(normResource.getEntity());
		List<ScoredEntity<String>> resourceDocs = resourceSearcher.search(Integer.toString(queryId));

		// Run normalization:
		// 1. Set the relevance of the result list.
		((CORI) normalization).setResultListRelevance(normResource.getScore());
		// 2. Normalize document scores
		List<ScoredEntity<String>> normDocs = normalization.normalize(resourceDocs);
		// 3. Merge with already processed results
		mergedResult = MergeSort.sort(mergedResult, normDocs);
	}
}
```



## [API Javadoc (link)](http://www.inf.usi.ch/phd/markov/LiDR-Javadoc)



## Implementation Details
The base package for LiDR is ```ch.usi.inf.lidr```. Resource selection, score normalization and results merging classes are located in ```selection```, ```normalization``` and ```merging``` sub-packages respectively. Corresponding tests can be found in the ```tests``` sub-package, while examples are located in the ```examples``` sub-package.


####Resource Selection
The UML class diagram for small-document (SD) approaches to resource selection can be found [here](https://github.com/markovi/LiDR/blob/master/src/uml/selection.png). The root of the class hierarchy is the ```ResourceSelection``` interface, which provides the ```select``` method. As an input, this method receives the sample ranking of documents and a list of corresponding sources. Each document is wrapped into a ```ScoredEntity``` object with a document identifier and a document relevance score. Each corresponding source is wrapped into a ```Resource``` object with a source identifier, the source size and the size of its sample. The ```select``` method outputs sources and their scores. Each output source is wrapped into a ```ScoredEntity``` object.

All SD resource selection techniques must implement the ```ResourceSelection``` interface by implementing the ```select``` method. In LiDR, SD techniques inherit this interface through the ```AbstractResourceSelection``` class, which contains the basic implementation of ```select``` and provides a number of other utility methods.

```AbstractResourceSelection``` is an abstract class, which cannot be used directly to perform resource selection. Instead, subclasses must implement its ```getResourceScores``` method. This is done by ```ReDDE```, ```CiSS``` and ```SUSHI```. ```ReDDE```, in turn, provides a method ```getScoreAtRank```, which computes a score that a source receives if its document appears in a sample ranking at a given position with a given score. This method can be overridden by subclasses (e.g. ```CRCS```, ```ReDDE.top``` and ```GAVG```).

In order to implement new SD resource selection techniques, the ```ResourceSelection``` interface can be extended directly or through the ```AbstractResourceSelection``` class (the later is easier but less flexible). Methods, which are computationally similar to ReDDE (e.g. CRCS and GAVG), can be implemented by extending the ```ReDDE``` class.


####Score Normalization and Results Merging
The UML class diagram for score normalization and results merging can be found [here](https://github.com/markovi/LiDR/blob/master/src/uml/norm.png). ```ScoreNormalization``` is the main interface, which provides the ```normalize``` method. This method receives a list of documents with unnormalized scores and returns the list of same documents with normalized scores. Each input and output document is wrapped into a ```ScoredEntity``` object.

The ```ScoreNormalization``` interface is implemented by the ```LinearScoreNormalization``` class. This class contains a basic implementation of the ```normalize``` method and provides a number of utility methods (e.g. for setting the length of a ranked list to be normalized). ```MinMax```, ```ZScore``` and ```Sum``` extend ```LinearScoreNormalization``` and implement the abstract ```doNormalization``` method. Other score normalization techniques can be implemented either by extending the ```ScoreNormalization``` interface or the ```LinearScoreNormalization``` class.

The ```ResultsMerging``` interface extends ```ScoreNormalization``` and serves as a wrapper for result merging techniques. ```ResultsMerging```  is directly implemented by ```CORI```, ```SSL``` and ```SAFE```. Note that each of these classes provide methods for setting additional parameters required to perform semi-supervised score normalization. For example, ```CORI``` allows to set its base normalization method (MinMax is used by default) and the value of its parameter. Also ```CORI``` requires setting the relevance of a ranked list (e.g. a corresponding resource selection score) before actually running normalization.

```SSL``` requires a sample ranking of documents to train a regression for a given query. This ranking should be set using the ```setSampleDocuments``` method for each query before running normalization.

To normalize document scores coming from a particular source, ```SAFE``` requires the ratio *R/S* for this source, where *R* and *S* are the source and sample sizes respectively. This ratio should be set using the ```setRankRatio``` method before running normalization. Similar to ```SSL```, ```SAFE``` also needs the sample ranking of documents for each query. This ranking should be set before normalizing document scores using the ```setSampleDocuments``` method.
