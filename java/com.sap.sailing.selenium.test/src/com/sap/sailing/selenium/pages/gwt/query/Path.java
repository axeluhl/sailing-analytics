package com.sap.sailing.selenium.pages.gwt.query;

/**
 * <p>Path represents a path expression.</p>
 * 
 * @author D049941
 *
 * @param <T>
 *   The type the path is bound to.
 */
public interface Path<T> extends Expression<T> {
    /**
     * <p>Returns the metadata for the path.</p>
     *
     * @return
     *   The metadata for the path.
     */
    public PathMetadata<?> getMetadata();

    /**
     * <p>Returns the root for this path.</p>
     *
     * @return
     *   The root for this path.
     */
    public Path<?> getRoot();
}
