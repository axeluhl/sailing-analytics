package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.Dimension;

public interface QuerySelectionProvider {

    public Map<Dimension, Collection<?>> getSelection();

}
