package com.sap.sse.util;

import java.io.Serializable;

/**
 * Use this instead of {@link Runnable} in conjunction with Lambda expressions if you need the
 * resulting Lambda object to be serializable.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <V>
 */
@FunctionalInterface
public interface SerializableRunnable extends Runnable, Serializable {
}
