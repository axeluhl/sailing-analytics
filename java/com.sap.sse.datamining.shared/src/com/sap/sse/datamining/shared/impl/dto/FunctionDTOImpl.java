package com.sap.sse.datamining.shared.impl.dto;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class FunctionDTOImpl implements FunctionDTO {
    private static final long serialVersionUID = 4587389541910498505L;

    private boolean isDimension;
    private String functionName;
    private String sourceTypeName;
    private String returnTypeName;
    private List<String> parameterTypeNames;

    private String displayName;
    private int ordinal;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    FunctionDTOImpl() {
    }
    
    public FunctionDTOImpl(boolean isDimension, String functionName, String sourceTypeName, String returnTypeName,
                           List<String> parameterTypeNames, String displayName, int ordinal) {
        this.isDimension = isDimension;
        this.functionName = functionName;
        this.sourceTypeName = sourceTypeName;
        this.returnTypeName = returnTypeName;
        this.parameterTypeNames = new ArrayList<String>(parameterTypeNames);
        
        this.displayName = displayName;
        this.ordinal = ordinal;
    }

    @Override
    public String getSourceTypeName() {
        return sourceTypeName;
    }

    @Override
    public String getReturnTypeName() {
        return returnTypeName;
    }

    @Override
    public List<String> getParameterTypeNames() {
        return parameterTypeNames;
    }
    
    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public boolean isDimension() {
        return isDimension;
    }
    
    @Override
    public int getOrdinal() {
        return ordinal;
    }
    
    @Override
    public int compareTo(FunctionDTO f) {
        return Integer.compare(this.getOrdinal(), f.getOrdinal());
    }
    
    @Override
    public String toString() {
        return (isDimension() ? "Dimension " : "Function ") + sourceTypeName + "." + functionName + "(" + parametersAsString() + ") : " + returnTypeName;
    }

    private String parametersAsString() {
        StringBuilder parameterBuilder = new StringBuilder();
        boolean first = true;
        for (String parameterTypeName : parameterTypeNames) {
            if (!first) {
                parameterBuilder.append(", ");
            }
            parameterBuilder.append(parameterTypeName);
            first = false;
        }
        return parameterBuilder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
        result = prime * result + (isDimension ? 1231 : 1237);
        result = prime * result + ((parameterTypeNames == null) ? 0 : parameterTypeNames.hashCode());
        result = prime * result + ((returnTypeName == null) ? 0 : returnTypeName.hashCode());
        result = prime * result + ((sourceTypeName == null) ? 0 : sourceTypeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionDTOImpl other = (FunctionDTOImpl) obj;
        if (functionName == null) {
            if (other.functionName != null)
                return false;
        } else if (!functionName.equals(other.functionName))
            return false;
        if (isDimension != other.isDimension)
            return false;
        if (parameterTypeNames == null) {
            if (other.parameterTypeNames != null)
                return false;
        } else if (!parameterTypeNames.equals(other.parameterTypeNames))
            return false;
        if (returnTypeName == null) {
            if (other.returnTypeName != null)
                return false;
        } else if (!returnTypeName.equals(other.returnTypeName))
            return false;
        if (sourceTypeName == null) {
            if (other.sourceTypeName != null)
                return false;
        } else if (!sourceTypeName.equals(other.sourceTypeName))
            return false;
        return true;
    }

}
