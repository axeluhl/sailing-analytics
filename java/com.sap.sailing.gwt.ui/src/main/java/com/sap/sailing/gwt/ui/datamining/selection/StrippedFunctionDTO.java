package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class StrippedFunctionDTO implements Comparable<StrippedFunctionDTO> {

    private final String returnTypeName;
    private final String displayName;
    private final List<String> parameterTypeNames;
    private final boolean isDimension;
    
    private final int ordinal;

    public StrippedFunctionDTO(FunctionDTO functionDTO) {
        returnTypeName = functionDTO.getReturnTypeName();
        displayName = functionDTO.getDisplayName();
        parameterTypeNames = new ArrayList<>(functionDTO.getParameterTypeNames());
        isDimension = functionDTO.isDimension();
        ordinal = functionDTO.getOrdinal();
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getOrdinal() {
        return ordinal;
    }
    
    @Override
    public int compareTo(StrippedFunctionDTO f) {
        return Integer.compare(this.getOrdinal(), f.getOrdinal());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + (isDimension ? 1231 : 1237);
        result = prime * result + ((parameterTypeNames == null) ? 0 : parameterTypeNames.hashCode());
        result = prime * result + ((returnTypeName == null) ? 0 : returnTypeName.hashCode());
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
        StrippedFunctionDTO other = (StrippedFunctionDTO) obj;
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
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
        return true;
    }

}
