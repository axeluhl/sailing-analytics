package com.sap.sse.gwt.dispatch.client;

import java.util.Collection;

public interface CollectionResult<T extends DTO> extends DTO, Result {
    Collection<T> getValues();
}
