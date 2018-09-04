package com.sap.sse.datamining.shared.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A data mining extraction function (Dimension or Statistic) that can be shared between client and server.
 * Instances are usually constructed by a DataMiningDTOFactory based on a given backend Function. The
 * corresponding backend instance can be determined using the DataMiningServer.
 */
public class FunctionDTO implements Serializable, Comparable<FunctionDTO> {
    private static final long serialVersionUID = 4587389541910498505L;

    private final boolean isDimension;
    /**
     * The functions name including its parameters in parenthesis or empty parenthesis, if this function doesn't have
     * parameters. Can consist of multiple concatenated function names, if the backend Function encapsulated multiple
     * data mining functions.
     */
    private final String functionName;
    /**
     * The fully qualified name of the type that declares this function.
     */
    private final String sourceTypeName;
    /**
     * The fully qualified name of the type returned by this function.
     */
    private final String returnTypeName;
    /**
     * The fully qualified names of the functions parameter types.
     */
    private final List<String> parameterTypeNames;

    /**
     * Meta-data for the natural ordering of FunctionDTOs. Should be omitted when persisting a FunctionDTO.
     */
    private final int ordinal;
    /**
     * A human readable string representation. Should be omitted when persisting a FunctionDTO.
     */
    private String displayName;
    /**
     * If the {@link #displayName} is not set and this provider is valid, the {@F
     */
    private transient DisplayNameProvider displayNameProvider;

    public static interface DisplayNameProvider {
        String getDisplayName();
    }
    
    /**
     * Makes the construction of the {@link #displayName} lazy, using the {@code displayNameProvider}.
     */
    public FunctionDTO(boolean isDimension, String functionName, String sourceTypeName, String returnTypeName,
            List<String> parameterTypeNames, DisplayNameProvider displayNameProvider, int ordinal) {
        this(isDimension, functionName, sourceTypeName, returnTypeName, parameterTypeNames,
                /* displayName is constructed lazily using the displayNameProvider */ (String) null, ordinal);
        this.displayNameProvider = displayNameProvider;
    }
    
    public FunctionDTO(boolean isDimension, String functionName, String sourceTypeName, String returnTypeName,
                           List<String> parameterTypeNames, String displayName, int ordinal) {
        this.isDimension = isDimension;
        this.functionName = functionName;
        this.sourceTypeName = sourceTypeName;
        this.returnTypeName = returnTypeName;
        this.parameterTypeNames = new ArrayList<String>(parameterTypeNames);
        
        this.displayName = displayName;
        this.ordinal = ordinal;
    }

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public List<String> getParameterTypeNames() {
        return parameterTypeNames;
    }

    /**
     * The functions name including its parameters in parenthesis or empty parenthesis, if this function doesn't have
     * parameters. Can consist of multiple concatenated function names, if the backend Function encapsulated multiple
     * data mining functions.
     * 
     * @return The functions name including its parameters in parenthesis or empty parenthesis, if this function doesn't
     *         have parameters.
     */
    public String getFunctionName() {
        return functionName;
    }

    public String getDisplayName() {
        if (displayName == null && displayNameProvider != null) {
            displayName = displayNameProvider.getDisplayName();
        }
        return displayName;
    }
    
    public boolean isDimension() {
        return isDimension;
    }
    
    public int getOrdinal() {
        return ordinal;
    }
    
    @Override
    public int compareTo(FunctionDTO f) {
        return Integer.compare(this.getOrdinal(), f.getOrdinal());
    }
    
    public String toString() {
        return (isDimension() ? "Dimension " : "Function ") + getSourceTypeName() + "." + getFunctionName() + " : " + getReturnTypeName();
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
        FunctionDTO other = (FunctionDTO) obj;
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
