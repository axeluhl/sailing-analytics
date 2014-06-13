package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTOImpl;

public class DeprecatedEnumsToFunctionDTOConverter {

    private static Map<StatisticType, FunctionDTO> statisticTypeToFunctionDTOMap = new HashMap<>();
    private static Map<DimensionIdentifier, FunctionDTO> dimensionIdentifierToFunctionDTOMap = new HashMap<>();
    
    static {
        for (StatisticType statisticType : StatisticType.values()) {
            FunctionDTO functionDTO = null;
            switch (statisticType) {
            case Distance:
                functionDTO = new FunctionDTOImpl("getDistanceTraveled", HasTrackedLegOfCompetitorContext.class.getSimpleName(), "Double",
                        new ArrayList<String>(), "Distance", false);
                break;
            case Speed:
                functionDTO = new FunctionDTOImpl("getGPSFix -> getSpeed -> getKnots", HasGPSFixContext.class.getSimpleName(),
                        "double", new ArrayList<String>(), "Speed", false);
                break;
            }
            statisticTypeToFunctionDTOMap.put(statisticType, functionDTO);
        }
        
        for (DimensionIdentifier dimensionIdentifier : DimensionIdentifier.values()) {
            FunctionDTO functionDTO = null;
            switch (dimensionIdentifier) {
            case BoatClassName:
                functionDTO = new FunctionDTOImpl("getBoatClass -> getName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Boat Class Name", true);
                break;
            case CompetitorName:
                functionDTO = new FunctionDTOImpl("getCompetitor -> getTeam -> getName", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Competitor Name", true);
                break;
            case CourseAreaName:
                functionDTO = new FunctionDTOImpl("getCourseArea -> getName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Course Area Name", true);
                break;
            case FleetName:
                functionDTO = new FunctionDTOImpl("getFleet -> getName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Fleet Name", true);
                break;
            case LegNumber:
                functionDTO = new FunctionDTOImpl("getLegNumber", HasTrackedLegContext.class.getSimpleName(),
                        "int", new ArrayList<String>(), "Leg Number", true);
                break;
            case LegType:
                functionDTO = new FunctionDTOImpl("getLegType", HasTrackedLegContext.class.getSimpleName(),
                        LegType.class.getSimpleName(), new ArrayList<String>(), "Leg Type", true);
                break;
            case Nationality:
                functionDTO = new FunctionDTOImpl("getCompetitor -> getTeam -> getNationality -> getThreeLetterIOCAcronym", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Nationality", true);
                break;
            case RaceName:
                functionDTO = new FunctionDTOImpl("getRace -> getName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Race Name", true);
                break;
            case RegattaName:
                functionDTO = new FunctionDTOImpl("getRegatta -> getName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Regattay Name", true);
                break;
            case SailID:
                functionDTO = new FunctionDTOImpl("getCompetitor -> getBoat -> getSailID", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Sail ID", true);
                break;
            case WindStrength:
                break;
            case Year:
                functionDTO = new FunctionDTOImpl("getYear", HasTrackedRaceContext.class.getSimpleName(),
                        "Integer", new ArrayList<String>(), "Year", true);
                break;
            }
            if (functionDTO != null) {
                dimensionIdentifierToFunctionDTOMap.put(dimensionIdentifier, functionDTO);
            }
        }
    }

    public static FunctionDTO getFunctionDTOFor(StatisticType statisticType) {
        return statisticTypeToFunctionDTOMap.get(statisticType);
    }

    public static FunctionDTO getFunctionDTOFor(DimensionIdentifier dimensionIdentifier) {
        return dimensionIdentifierToFunctionDTOMap.get(dimensionIdentifier);
    }
    
    private DeprecatedEnumsToFunctionDTOConverter() {
    }

}
