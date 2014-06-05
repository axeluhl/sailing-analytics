package com.sap.sailing.datamining;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Moving;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.WithNationality;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.factories.FunctionFactory;
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

    private static void initializeDeprecatedEnumsToFunctionDTOMaps() {
        for (StatisticType statisticType : StatisticType.values()) {
            FunctionDTO functionDTO = null;
            switch (statisticType) {
            case Distance:
                functionDTO = new FunctionDTOImpl("Distance", HasGPSFixContext.class.getSimpleName(), "double",
                        new ArrayList<String>(), "Distance", false);
                break;
            case Speed:
                functionDTO = new FunctionDTOImpl("Speed", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        "double", new ArrayList<String>(), "Speed", false);
                break;
            }
            statisticTypeToFunctionDTOMap.put(statisticType, functionDTO);
        }
        
        for (DimensionIdentifier dimensionIdentifier : DimensionIdentifier.values()) {
            FunctionDTO functionDTO = null;
            switch (dimensionIdentifier) {
            case BoatClassName:
                functionDTO = new FunctionDTOImpl("BoatClassName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Boat Class Name", true);
                break;
            case CompetitorName:
                functionDTO = new FunctionDTOImpl("CompetitorName", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Competitor Name", true);
                break;
            case CourseAreaName:
                functionDTO = new FunctionDTOImpl("CourseClassName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Course Area Name", true);
                break;
            case FleetName:
                functionDTO = new FunctionDTOImpl("FleetName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Fleet Name", true);
                break;
            case LegNumber:
                functionDTO = new FunctionDTOImpl("LegNumber", HasTrackedLegContext.class.getSimpleName(),
                        "int", new ArrayList<String>(), "Leg Number", true);
                break;
            case LegType:
                functionDTO = new FunctionDTOImpl("LegType", HasTrackedLegContext.class.getSimpleName(),
                        LegType.class.getSimpleName(), new ArrayList<String>(), "Leg Type", true);
                break;
            case Nationality:
                functionDTO = new FunctionDTOImpl("Nationality", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Nationality", true);
                break;
            case RaceName:
                functionDTO = new FunctionDTOImpl("RaceName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Race Name", true);
                break;
            case RegattaName:
                functionDTO = new FunctionDTOImpl("RegattaName", HasTrackedRaceContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Regattay Name", true);
                break;
            case SailID:
                functionDTO = new FunctionDTOImpl("SailID", HasTrackedLegOfCompetitorContext.class.getSimpleName(),
                        String.class.getSimpleName(), new ArrayList<String>(), "Sail ID", true);
                break;
            case WindStrength:
                break;
            case Year:
                functionDTO = new FunctionDTOImpl("Year", HasTrackedRaceContext.class.getSimpleName(),
                        "int", new ArrayList<String>(), "Year", true);
                break;
            }
            if (functionDTO != null) {
                dimensionIdentifierToFunctionDTOMap.put(dimensionIdentifier, functionDTO);
            }
        }
    }

    private static void initializeFunctionDTOToFunctionMap() {
        for (StatisticType statisticType : StatisticType.values()) {
            FunctionDTO functionDTO = getFunctionDTOFor(statisticType);
            Method method = null;
            Function<?> function = null;
            switch (statisticType) {
            case Distance:
                method = getNullaryMethodFromClass(HasTrackedLegOfCompetitorContext.class, "getDistanceTraveled");
                function = FunctionFactory.createMethodWrappingFunction(method);
                break;
            case Speed:
                method = getNullaryMethodFromClass(HasGPSFixContext.class, "getGPSFix");
                Function<?> getGPSFix = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Moving.class, "getSpeed");
                Function<?> getSpeed = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Speed.class, "getKnots");
                Function<?> getKnots = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Speed", Arrays.asList(getGPSFix, getSpeed, getKnots));
                break;
            }
            functionDTOToFunctionMap.put(functionDTO, function);
        }
        
        for (DimensionIdentifier dimensionIdentifier : DimensionIdentifier.values()) {
            FunctionDTO functionDTO = getFunctionDTOFor(dimensionIdentifier);
            Method method = null;
            Function<?> function = null;

            method = getNullaryMethodFromClass(Named.class, "getName");
            Function<?> getName = FunctionFactory.createMethodWrappingFunction(method);
            switch (dimensionIdentifier) {
            case BoatClassName:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getBoatClass");
                Function<?> getBoatClass = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Boat Class Name",
                        Arrays.asList(getBoatClass, getName));
                break;
            case CompetitorName:
                method = getNullaryMethodFromClass(HasTrackedLegOfCompetitorContext.class, "getCompetitor");
                Function<?> getCompetitor = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Competitor Name",
                        Arrays.asList(getCompetitor, getName));
                break;
            case CourseAreaName:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getCourseArea");
                Function<?> getCourseArea = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Course Area Name",
                        Arrays.asList(getCourseArea, getName));
                break;
            case FleetName:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getFleet");
                Function<?> getFleet = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Fleet Name", Arrays.asList(getFleet, getName));
                break;
            case LegNumber:
                method = getNullaryMethodFromClass(HasTrackedLegContext.class, "getLegNumber");
                function = FunctionFactory.createMethodWrappingFunction(method);
                break;
            case LegType:
                method = getNullaryMethodFromClass(HasTrackedLegContext.class, "getLegType");
                function = FunctionFactory.createMethodWrappingFunction(method);
                break;
            case Nationality:
                method = getNullaryMethodFromClass(HasTrackedLegOfCompetitorContext.class, "getCompetitor");
                getCompetitor = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Competitor.class, "getTeam");
                Function<?> getTeam = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(WithNationality.class, "getNationality");
                Function<?> getNationality = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Nationality.class, "getThreeLetterIOCAcronym");
                Function<?> getNationalityAcronym = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Nationality",
                        Arrays.asList(getCompetitor, getTeam, getNationality, getNationalityAcronym));
                break;
            case RaceName:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getRace");
                Function<?> getRace = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Race Name", Arrays.asList(getRace, getName));
                break;
            case RegattaName:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getRegatta");
                Function<?> getRegatta = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Regatta Name", Arrays.asList(getRegatta, getName));
                break;
            case SailID:
                method = getNullaryMethodFromClass(HasTrackedLegOfCompetitorContext.class, "getCompetitor");
                getCompetitor = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Competitor.class, "getBoat");
                Function<?> getBoat = FunctionFactory.createMethodWrappingFunction(method);
                method = getNullaryMethodFromClass(Boat.class, "getSailID");
                Function<?> getSailID = FunctionFactory.createMethodWrappingFunction(method);
                function = FunctionFactory.createCompoundFunction("Sail ID", Arrays.asList(getCompetitor, getBoat, getSailID));
                break;
            case WindStrength:
                break;
            case Year:
                method = getNullaryMethodFromClass(HasTrackedRaceContext.class, "getYear");
                function = FunctionFactory.createMethodWrappingFunction(method);
                break;
            }
            if (function != null) {
                functionDTOToFunctionMap.put(functionDTO, function);
            }
        }
    }

    private static Method getNullaryMethodFromClass(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName, new Class<?>[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException("The class " + clazz.getSimpleName() + " doesn't have"
                    + " the nullary method " + methodName);
        }
    }

    public static FunctionDTO getFunctionDTOFor(StatisticType statisticType) {
        return statisticTypeToFunctionDTOMap.get(statisticType);
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
