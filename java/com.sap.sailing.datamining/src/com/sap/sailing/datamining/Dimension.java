package com.sap.sailing.datamining;

import java.util.Collection;

public interface Dimension {

    public SelectionCriteria createCriteria(Collection<String> selection);

}
