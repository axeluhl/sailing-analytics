package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.DistanceInMetersExtractor;
import com.sap.sailing.datamining.shared.ExtractorType;

public class ExtractorFactory {

    public static Extractor createDistanceInMetersExtractor() {
        return new DistanceInMetersExtractor();
    }
    
    public static Extractor createExtractor(ExtractorType extractorType) {
        switch (extractorType) {
        case DistanceInMeters:
            return createDistanceInMetersExtractor();
        default:
            throw new IllegalArgumentException("Case for the extractor type '" + extractorType + "' isn't implemented.");
        }
    }
    
}
