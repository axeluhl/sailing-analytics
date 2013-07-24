package com.sap.sailing.datamining;

public interface SelectionCriteria {

    boolean matches(SelectionContext context);

    DataRetriever getDataRetriever(SelectionContext context);

}
