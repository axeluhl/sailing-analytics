package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

abstract class ExtractionFunctionWithContext implements Comparable<ExtractionFunctionWithContext> {

    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final FunctionDTO extractionFunction;
    private final Collection<String> matchingStrings;

    protected ExtractionFunctionWithContext(DataRetrieverChainDefinitionDTO retrieverChain,
            FunctionDTO extractionFunction) {
        this.retrieverChain = retrieverChain;
        this.extractionFunction = extractionFunction;
        matchingStrings = new ArrayList<>(4);
    }

    public DataRetrieverChainDefinitionDTO getRetrieverChain() {
        return retrieverChain;
    }

    public FunctionDTO getExtractionFunction() {
        return extractionFunction;
    }
    
    public abstract String getDisplayString();
    
    public abstract String getAdditionalDisplayString();

    public Iterable<String> getMatchingStrings() {
        return matchingStrings;
    }
    
    protected void addMatchingString(String matchingString) {
        matchingStrings.add(matchingString);
    }

    @Override
    public int compareTo(ExtractionFunctionWithContext o) {
        String otherDisplayName = o.getExtractionFunction().getDisplayName();
        int comparedDisplayName = extractionFunction.getDisplayName().compareToIgnoreCase(otherDisplayName);
        if (comparedDisplayName != 0) {
            return comparedDisplayName;
        }
        return retrieverChain.compareTo(o.getRetrieverChain());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extractionFunction == null) ? 0 : extractionFunction.hashCode());
        result = prime * result + ((retrieverChain == null) ? 0 : retrieverChain.hashCode());
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
        ExtractionFunctionWithContext other = (ExtractionFunctionWithContext) obj;
        if (extractionFunction == null) {
            if (other.extractionFunction != null)
                return false;
        } else if (!extractionFunction.equals(other.extractionFunction))
            return false;
        if (retrieverChain == null) {
            if (other.retrieverChain != null)
                return false;
        } else if (!retrieverChain.equals(other.retrieverChain))
            return false;
        return true;
    }

}