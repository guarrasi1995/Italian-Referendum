# Italian-Referendum

0\) Temporal Analysis

1. Manually/Semi-Automatically collect on the web (political party website, institutional website, wikipedia, public available list of politicians) and collect all possible italian politics (or journalists) name/twitter account P; divide them in two group according to their support to Yes Y or No N (skip otherwise); How many users you get? How many tweets? Which is their distribution over time?.

2. For each p in Y | N analyze all the tweets/retweet T(Y) and build SAX string (grain = 12h) for the Top 1000 words by frequencies that expose the typical pattern that capture the collective attention (see slides SAX* ); group together (implementing a trivial K-Means) all the strings that expose an equal temporal behaviour (same or very similar SAX string) t_1(Y), t_2(Y), t_1(N), t_2(N)...; (Clusters of terms)

3. For each group of token in t_i(Y) and in t_j(N) build the co-occurrence graph of them (two word t_1, t_2 have an edge e, if they appear in the same document; the weight w of e is equal to the number of documents where both token appear); using a threshold over edge weights (decide which one produce best results) , identifying the Connected Components CC and extract the innermost core ( K-Core ) from each of them, producing subgroup of tokens, t_1’(Y), t_2’’(Y).Comment about the differences and decide which is the best strategy K-Core vs Simple Connected Component.

4. Using the original statistics (collection), trace the time series (grain 3h) for each obtained group of token t_i’(Y), t_j’(N); compare (manually) the time series of each group Y and N and comments about some possible kind of action-reaction that should be clearly identified. (look also at the content of the tweets)

1\) Identify mentions of candidates or YES/NO supporter

1. From the entire tweets dataset, identify tweets of users that mention one of the politicians (include also the previously founded P account) that support YES or NO or directly express their opinion about the referendum (use also t_i’(Y), t_j’(N) groups of words). How many users you get? How many tweets? Let M be the set of such users and let T(M) be the set of related tweets.

2. Using the provided Graph and the library G (see slides to obtain it) first select the subgraph induced by users S(M) then find the largest connected component CC and compute HITS on this subgraph. Then (in the next step 1.3), find the 2000 highest ranked (Authority) users. Who are they? Can be divided in YES and NO supporters? Propose a metric.

3. Partitioning the users of M proposing a metric that take in consideration the candidates they mention (each user can mention more that one candidate more than one time). Identify the users mentioning more frequently each candidate or support YES/NO and measure their centrality (Hubness Authority). Find the 1000 (for each party YES/NO) who both highly support the candidates and are highly central (define some combined measure to select such candidates and propose a (trivial) method to give sentiment to those mentions). We define these users (1000+1000) as M' in M.

4. Identify for each option YES / NO which are the top 500 k-Players K using the KPP-NEG algorithm on S(M). (if it is necessary reduce the original graph removing those nodes with a degree lower than a selected threshold (48h of processing or so)).

2\) Spread of Influence

1. Creating a version of the K-Mean that works with G library (using it.stilo.g.util.ArraysUtil may helps) . The membership function (Similarity) of the K-Mean algorithm must be based not only on the neighborhood of the considered node but also on the labels (YES/NO) of their neighborhoods and of the centroid (propose your solution). The algorithm must be initialised with two clusters (K=2) containing all the SEEDS nodes identified (for every party) at the previous step (1 of this documents). Using the proposed algorithm to evaluate which is the spread of the two parties over the network S(M) , using the following SEEDS:
– only the identified k-Players K are used as seeds of the modified LPA?
– Using M
– Using only the M’

3\) Addendum

1. Using a modified version of LPA (Label Propagation Algorithm start from the provided one in the G library) that assigning a label only for those users that are classified with YES or NO estimates over the whole network which party spread more over the network. How is the spread over the network if: only the identified k-Players K are used as seeds of the modified LPA?

2. Running LPA severals times (10) Using M to find subcommunities.

3. Propose a function (over the LPA runs) to decide finally to which community a user u belongs to.

4. Implements the NMI Normalized Mutual Information measure and plot the matrix that represent the NMI between the 10 LPA runs.

4\) Dataset

The provided Dataset contain a generated network composed by TwitterID (one edge per line src <tab> dst <tab> weight ):
Official_SBN-ITA-2016-Net.gz and the folder stream that containing the stream across 4th of December divided by day; each files contains 10000 tweets. Use the StatusWrapper.java class to read it. The dataset (around 10Gb) is Available using Resilio Sync Software: https://www.resilio.com/individuals/ with the following key: BGO6J6CQDRKBEDG3HRFXRN45TLXST3ERW
