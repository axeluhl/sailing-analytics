package com.sap.sse.security.shared;

import java.io.Serializable;

/**
 * Some annotation on an object that is identified by a stringified ID and for which an optional display name can be
 * provided, mostly for debugging and maybe for UI purposes.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public interface ObjectAnnotation<T extends Serializable> extends Serializable {
    String getIdOfAnnotatedObjectAsString();
    String getDisplayNameOfAnnotatedObject();
    T getAnnotation();
}
