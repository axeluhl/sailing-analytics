package com.sap.sailing.datamining;

import java.util.Collection;

public interface SelectionCriteria {

    boolean matches(SelectionContext context);

    DataRetriever getDataRetriever(SelectionContext context);

    Collection<GPSFixWithContext> getDataIfMatches(SelectionContext context);

}
