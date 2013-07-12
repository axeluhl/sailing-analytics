package com.sap.sailing.datamining.shared;

import com.sap.sailing.datamining.impl.DistanceInMetersExtractor;

public class ExtractorFactory {

    public static Extractor createDistanceInMetersExtractor() {
        return new DistanceInMetersExtractor();
    }
    
}
