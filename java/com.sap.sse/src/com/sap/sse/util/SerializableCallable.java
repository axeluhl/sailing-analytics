package com.sap.sse.util;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Use this instead of {@link Callable} in conjunction with Lambda expressions if you need the
 * resulting Lambda object to be serializable.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <V>
 */
@FunctionalInterface
public interface SerializableCallable<V> extends Callable<V>, Serializable {
}
