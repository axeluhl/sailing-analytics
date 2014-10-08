package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.List;

public interface FunctionDTO extends Serializable, Comparable<FunctionDTO> {

    public boolean isDimension();

    public String getDisplayName();

    public List<String> getParameters();

    public String getReturnTypeName();

    public String getSourceTypeName();
    
    public int getOrdinal();

}
