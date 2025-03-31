package com.sap.sailing.domain.common.racelog.tracking;

import com.sap.sse.common.TransformationException;

/**
 * Handle transformation between two types. Most likely use-case is serialization,
 * e.g. for JSON documents or database persistence.
 * 
 * @author Fredrik Teschke
 *
 */
public interface TransformationHandler<T1, T2> {
    T1 transformBack(T2 object) throws TransformationException;
    T2 transformForth(T1 object) throws TransformationException;
}
