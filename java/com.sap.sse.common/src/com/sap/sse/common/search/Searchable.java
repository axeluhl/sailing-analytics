package com.sap.sse.common.search;

/**
 * Some data repository that can be searched with a search query, delivering a search result.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Searchable<H extends Hit> {
    Result<H> query(Query query);
}
