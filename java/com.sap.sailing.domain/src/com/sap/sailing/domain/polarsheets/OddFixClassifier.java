package com.sap.sailing.domain.polarsheets;


public interface OddFixClassifier {
    
    /**
     * Used for classifying fixes from a logical perspective. 
     * 
     * Example: An OddFixClassifier could check if a boat has high speed when going against the wind.
     * 
     * @param polarFix to be classified
     * @return True if odd from a sailing perspective, false if it makes sense. Classification depends on the type of OddFixClassifier used.
     */
    public boolean classifiesAsOdd(PolarFix polarFix);

}
