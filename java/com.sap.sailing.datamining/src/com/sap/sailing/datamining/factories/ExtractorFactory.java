package com.sap.sailing.datamining.factories;

import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.impl.DataAmountExtractor;
import com.sap.sailing.datamining.impl.SpeedInKnotsExtractor;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.domain.base.Moving;

public final class ExtractorFactory {
    
    private ExtractorFactory() { }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ExtractedType extends Number> Extractor<DataType, ExtractedType> createExtractor(StatisticType statisticType) {
        switch (statisticType) {
        case DataAmount:
            return (Extractor<DataType, ExtractedType>) createDataAmountExtractor();
        case Speed:
            return (Extractor<DataType, ExtractedType>) createSpeedExtractor();
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistic type: "
                + statisticType.toString());
    }
    
    public static Extractor<Moving, Double> createSpeedExtractor() {
        return new SpeedInKnotsExtractor();
    }

    public static <DataType> Extractor<DataType, Integer> createDataAmountExtractor() {
        return new DataAmountExtractor<DataType>();
    }

}
