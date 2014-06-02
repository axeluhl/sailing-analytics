package com.sap.sse.common.search;

/**
 * Result of a search.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Result {
    /**
     * The query for which this is the result. Useful when multiple queries have been fired concurrently.
     */
    Query getQuery();
}
