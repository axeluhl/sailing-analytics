package com.sap.sailing.gwt.dispatch.client;

import java.util.Collection;

public interface CollectionResult<T extends DTO> extends DTO, Result {
    Collection<T> getValues();
}
