package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.impl.data.GPSFixWithContext;
import com.sap.sailing.datamining.impl.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;

public class DeprecatedToFunctionConverter {

    private static Map<StatisticType, FunctionDTO> statisticTypeToFunctionDTOMap = new HashMap<>();
    private static Map<DimensionIdentifier, FunctionDTO> dimensionIdentifierToFunctionDTOMap = new HashMap<>();
    
    private static Map<FunctionDTO, Function<?>> functionDTOToFunctionMap = new HashMap<>();
    
    static {
        initializeDeprecatedEnumsToFunctionDTOMaps();
        initializeFunctionDTOToFunctionMap();
    }

    public static FunctionDTO getFunctionDTOFor(StatisticType statisticType) {
        return statisticTypeToFunctionDTOMap.get(statisticType);
    }

    private static void initializeDeprecatedEnumsToFunctionDTOMaps() {
        for (StatisticType statisticType : StatisticType.values()) {
            FunctionDTO functionDTO = null;
            switch (statisticType) {
            case Distance:
                functionDTO = new FunctionDTOImpl("Distance", GPSFixWithContext.class.getSimpleName(), "double", new ArrayList<String>(), "Distance", false);
                break;
            case Speed:
                functionDTO = new FunctionDTOImpl("Speed", TrackedLegOfCompetitorWithContext.class.getSimpleName(), "double", new ArrayList<String>(), "Speed", false);
                break;
            }
            statisticTypeToFunctionDTOMap.put(statisticType, functionDTO);
        }
    }

    private static void initializeFunctionDTOToFunctionMap() {
        for (DimensionIdentifier dimensionIdentifier : DimensionIdentifier.values()) {
//            FunctionDTO functionDTO = null;
            switch (dimensionIdentifier) {
            case BoatClassName:
                break;
            case CompetitorName:
                break;
            case CourseAreaName:
                break;
            case FleetName:
                break;
            case LegNumber:
                break;
            case LegType:
                break;
            case Nationality:
                break;
            case RaceName:
                break;
            case RegattaName:
                break;
            case SailID:
                break;
            case WindStrength:
                break;
            case Year:
                break;
            }
            dimensionIdentifierToFunctionDTOMap.put(dimensionIdentifier, functionDTO);
        }
    }

    public static FunctionDTO getFunctionDTOFor(DimensionIdentifier dimensionIdentifier) {
        return dimensionIdentifierToFunctionDTOMap.get(dimensionIdentifier);
    }
    
    public static Function<?> getFunctionFor(FunctionDTO functionDTO) {
        return functionDTOToFunctionMap.get(functionDTO);
    }
    
    private DeprecatedToFunctionConverter() {
    }

}
