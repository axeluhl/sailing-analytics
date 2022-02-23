package com.sap.sse.datamining.shared.impl.dto.parameters;

import java.io.Serializable;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class WildcardParameter extends AbstractParameterizedDimensionFilter {
    private static final long serialVersionUID = -6080128968220636466L;

    public WildcardParameter() { }

    public WildcardParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
        super(retrieverLevel, dimension);
    }

    @Override
    public boolean matches(Serializable value) {
        return true;
    }

}
