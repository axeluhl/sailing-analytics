package com.sap.sse.gwt.dispatch.client;

import java.util.Collection;

/**
 * Collection result that can be reused to send a collection of {@link DTO} instances
 *
 * @param <T>
 */
public interface CollectionResult<T extends DTO> extends DTO, Result {
    Collection<T> getValues();
}
