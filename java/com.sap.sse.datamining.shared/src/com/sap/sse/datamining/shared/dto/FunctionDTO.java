package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.List;

public interface FunctionDTO extends Serializable, Comparable<FunctionDTO> {

    public String getSourceTypeName();

    public String getReturnTypeName();

    public String getFunctionName();

    public String getDisplayName();

    public List<String> getParameterTypeNames();

    public boolean isDimension();
    
    public int getOrdinal();

}
