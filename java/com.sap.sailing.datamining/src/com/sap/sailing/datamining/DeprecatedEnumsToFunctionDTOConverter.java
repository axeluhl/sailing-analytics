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
                functionDTO = new FunctionDTOImpl(false, "getDistanceTraveled", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        "Double", new ArrayList<String>(), "Distance", 0);
                break;
            case Speed:
                functionDTO = new FunctionDTOImpl(false, "getGPSFix -> getSpeed -> getKnots",
                        HasGPSFixContext.class.getSimpleName(), "double", new ArrayList<String>(), "Speed", 0);
                break;
            }
            statisticTypeToFunctionDTOMap.put(statisticType, functionDTO);
        }
        
        for (DimensionIdentifier dimensionIdentifier : DimensionIdentifier.values()) {
            FunctionDTO functionDTO = null;
            switch (dimensionIdentifier) {
            case BoatClassName:
                functionDTO = new FunctionDTOImpl(true, "getBoatClass -> getName",
                        HasTrackedRaceContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Boat Class Name", 0);
                break;
            case CompetitorName:
                functionDTO = new FunctionDTOImpl(true, "getCompetitor -> getTeam -> getName",
                        HasTrackedLegOfCompetitorContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Competitor Name", 0);
                break;
            case CourseAreaName:
                functionDTO = new FunctionDTOImpl(true, "getCourseArea -> getName",
                        HasTrackedRaceContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Course Area Name", 0);
                break;
            case FleetName:
                functionDTO = new FunctionDTOImpl(true, "getFleet -> getName",
                        HasTrackedRaceContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Fleet Name", 0);
                break;
            case LegNumber:
                functionDTO = new FunctionDTOImpl(true, "getLegNumber",
                        HasTrackedLegContext.class.getSimpleName(), "int", new ArrayList<String>(), "Leg Number", 0);
                break;
            case LegType:
                functionDTO = new FunctionDTOImpl(true, "getLegType",
                        HasTrackedLegContext.class.getSimpleName(), LegType.class.getSimpleName(), new ArrayList<String>(), "Leg Type", 0);
                break;
            case Nationality:
                functionDTO = new FunctionDTOImpl(true, "getCompetitor -> getTeam -> getNationality -> getThreeLetterIOCAcronym",
                        HasTrackedLegOfCompetitorContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Nationality", 0);
                break;
            case RaceName:
                functionDTO = new FunctionDTOImpl(true, "getRace -> getName",
                        HasTrackedRaceContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Race Name", 0);
                break;
            case RegattaName:
                functionDTO = new FunctionDTOImpl(true, "getRegatta -> getName",
                        HasTrackedRaceContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Regattay Name", 0);
                break;
            case SailID:
                functionDTO = new FunctionDTOImpl(true, "getCompetitor -> getBoat -> getSailID",
                        HasTrackedLegOfCompetitorContext.class.getSimpleName(), String.class.getSimpleName(), new ArrayList<String>(), "Sail ID", 0);
                break;
            case Year:
                functionDTO = new FunctionDTOImpl(true, "getYear",
                        HasTrackedRaceContext.class.getSimpleName(), "Integer", new ArrayList<String>(), "Year", 0);
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
