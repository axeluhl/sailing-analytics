package com.sap.sse.datamining.ui.client.parameterization;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface ParameterizedFilterDimension {
    DataRetrieverLevelDTO getRetrieverLevel();
    FunctionDTO getDimension();
    
    Collection<? extends Serializable> getAvailableValues(Iterable<? extends Serializable> allValues);
    boolean matches(Serializable value);
}
