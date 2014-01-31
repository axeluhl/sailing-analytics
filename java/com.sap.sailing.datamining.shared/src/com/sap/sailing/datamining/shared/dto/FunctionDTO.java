package com.sap.sailing.datamining.shared.dto;

import java.io.Serializable;
import java.util.List;

public interface FunctionDTO extends Serializable {

    public abstract boolean isDimension();

    public abstract String getDisplayName();

    public abstract List<String> getParameters();

    public abstract String getReturnTypeName();

    public abstract String getSourceTypeName();

}
