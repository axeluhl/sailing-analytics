package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Collection;

public interface CollectionResult<T extends DTO> extends DTO, Result {
    Collection<T> getValues();
}
