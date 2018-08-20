package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.datamining.SailingPredefinedQueries;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.RestServletContainer;
import com.sap.sailing.server.gateway.serialization.NotJsonSerializableException;
import com.sap.sse.common.Distance;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.AverageWithStats;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

/**
 * REST-API to run predefined queries and get the result as JSON.
 * @author Lennart Hensler (D054527)
 */
@Path("/v1/datamining")
public class DataMiningResource extends AbstractSailingServerResource {
    
    private SailingPredefinedQueries predefinedDataMiningQueries;

    private static final Function<Distance, Number> distanceMetersExtractor = (distance) -> distance.getMeters();
    
    public final static String QUERY_AVERAGE_SPEED_PER_REGATTA_RACE = "AvgSpeed_Per_Regatta-Race";
    public final static String QUERY_AVERAGE_SPEED_PER_COMPETITOR_LEGTYPE = "AvgSpeed_Per_Competitor-LegType";
    public final static String QUERY_AVERAGE_SPEED_PER_COMPETITOR = "AvgSpeed_Per_Competitor";
    public final static String QUERY_DISTANCE_TRAVELED_PER_COMPETITOR_LEGTYPE = "DistanceTraveled_Per_Competitor-LegType";
    public final static String QUERY_DISTANCE_TRAVELED_PER_COMPETITOR = "DistanceTraveled_Per_Competitor";
    public final static String QUERY_MANEUVERS_PER_COMPETITOR = "Maneuvers_Per_Competitor";

    public List<PredefinedQueryIdentifier> getPredefinedRegattaDataMiningQueries() {
        return getPredefinedQueriesByNames(
                QUERY_AVERAGE_SPEED_PER_COMPETITOR_LEGTYPE,
                QUERY_AVERAGE_SPEED_PER_COMPETITOR,
                QUERY_DISTANCE_TRAVELED_PER_COMPETITOR_LEGTYPE,
                QUERY_DISTANCE_TRAVELED_PER_COMPETITOR,
                QUERY_MANEUVERS_PER_COMPETITOR);
    }

    public List<PredefinedQueryIdentifier> getPredefinedRaceDataMiningQueries() {
        return getPredefinedQueriesByNames(
                QUERY_AVERAGE_SPEED_PER_REGATTA_RACE, 
                QUERY_AVERAGE_SPEED_PER_COMPETITOR_LEGTYPE,
                QUERY_AVERAGE_SPEED_PER_COMPETITOR,
                QUERY_DISTANCE_TRAVELED_PER_COMPETITOR_LEGTYPE,
                QUERY_DISTANCE_TRAVELED_PER_COMPETITOR,
                QUERY_MANEUVERS_PER_COMPETITOR);
    }

    public List<PredefinedQueryIdentifier> getPredefinedQueriesByNames(String... names) {
        List<PredefinedQueryIdentifier> result = new ArrayList<>();
        SailingPredefinedQueries predefinedMiningQueries = getPredefinedMiningQueries();
        for(PredefinedQueryIdentifier id: predefinedMiningQueries.getQueries().keySet()) {
            for(String name: names) {
                if(id.getIdentifier().equals(name)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    public DataMiningServer getDataMiningServer() {
        @SuppressWarnings("unchecked")
        ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker = (ServiceTracker<DataMiningServer, DataMiningServer>) getServletContext().getAttribute(RestServletContainer.DATA_MINING_SERVER_TRACKER_NAME);
        return dataMiningServerTracker.getService(); 
    }

    private Response getBadIdentifierErrorResponse(String identifier) {
        return Response.status(Status.NOT_FOUND).entity("Couldn't find a predefined query with the identifier '" + identifier + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadQueryDefinitionErrorResponse(ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO) {
        return Response.status(Status.BAD_REQUEST).entity("Couldn't create a query for definition '" + StringEscapeUtils.escapeHtml(queryDefinitionDTO.toString()) + "'.")
                .type(MediaType.TEXT_PLAIN).build();
    }

    private Response getNoDataFoundErrorResponse() {
        return Response.status(Status.NOT_FOUND).entity("No data found.").type(MediaType.TEXT_PLAIN).build();
    }
    
    private Response getNotSerializableErrorResponse(Class<?> type) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Can't serialize values of type '" + type.getName() + "'.").
                type(MediaType.TEXT_PLAIN).build();
    }

    private SailingPredefinedQueries getPredefinedMiningQueries() {
        if (predefinedDataMiningQueries == null) {
            predefinedDataMiningQueries = new SailingPredefinedQueries();
        }
        return predefinedDataMiningQueries;
    }

    public Response predefinedQueryIdentifiersToJSON(Iterable<PredefinedQueryIdentifier> identifiers) {
        JSONArray predefinedQueryNames = new JSONArray();
        for (PredefinedQueryIdentifier identifier : identifiers) {
            JSONObject jsonId = new JSONObject();
            jsonId.put("Identifier", identifier.getIdentifier());
            jsonId.put("Description", identifier.getDescription());
            predefinedQueryNames.add(jsonId);
        }
        
        String json = predefinedQueryNames.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    public Response avgSpeedPerCompetitorAndLegType(String regattaName) {
        return avgSpeedPerCompetitorQuery(SailingPredefinedQueries.QUERY_AVERAGE_SPEED_PER_COMPETITOR_LEGTYPE, regattaName);
    }

    public Response avgSpeedPerCompetitor(String regattaName) {
        return avgSpeedPerCompetitorQuery(SailingPredefinedQueries.QUERY_AVERAGE_SPEED_PER_COMPETITOR, regattaName);
    }

    public Response avgSpeedPerCompetitorAndLegType(String regattaName, String raceName) {
        return avgSpeedPerCompetitorQuery(SailingPredefinedQueries.QUERY_AVERAGE_SPEED_PER_COMPETITOR_LEGTYPE, regattaName, raceName);
    }

    public Response avgSpeedPerCompetitor(String regattaName, String raceName) {
        return avgSpeedPerCompetitorQuery(SailingPredefinedQueries.QUERY_AVERAGE_SPEED_PER_COMPETITOR, regattaName, raceName);
    }

    private Response avgSpeedPerCompetitorQuery(String predefinedQueriesName, String regattaName) {
        return avgSpeedPerCompetitorQuery(predefinedQueriesName, regattaName, null);
    }

    private Response avgSpeedPerCompetitorQuery(String predefinedQueriesName, String regattaName, String raceName) {
        Response response;
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = getPredefinedQuery(predefinedQueriesName);
        
        if (queryDefinitionDTO == null) {
            response = getBadIdentifierErrorResponse(predefinedQueriesName);
        } else {
            HashMap<FunctionDTO, HashSet<? extends Serializable>> race_FilterSelection = new HashMap<>();

            FunctionDTO getRegattaName = new FunctionDTO(true, "getRegatta().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
            HashSet<Serializable> getRegattaName_FilterSelection = new HashSet<>();
            getRegattaName_FilterSelection.add(regattaName);
            race_FilterSelection.put(getRegattaName, getRegattaName_FilterSelection);

            if(raceName != null) {
                FunctionDTO getRaceName = new FunctionDTO(true, "getRace().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
                HashSet<Serializable> getRaceName_FilterSelection = new HashSet<>();
                getRaceName_FilterSelection.add(raceName);

                race_FilterSelection.put(getRaceName, getRaceName_FilterSelection);
            }
            queryDefinitionDTO.setFilterSelectionFor(queryDefinitionDTO.getDataRetrieverChainDefinition().getRetrieverLevel(2), race_FilterSelection);
            response = runQuery(queryDefinitionDTO, regattaName, raceName, (AverageWithStats<Double> aws)->aws.getAverage(), "kn", 2);
        }
        return response;
    }

    public Response sumDistanceTraveledPerCompetitor(String regattaName) {
        return sumDistanceTraveledPerCompetitorQuery(SailingPredefinedQueries.QUERY_DISTANCE_TRAVELED_PER_COMPETITOR, regattaName);
    }

    public Response sumDistanceTraveledPerCompetitorAndLegType(String regattaName) {
        return sumDistanceTraveledPerCompetitorQuery(SailingPredefinedQueries.QUERY_DISTANCE_TRAVELED_PER_COMPETITOR_LEGTYPE, regattaName);
    }
    
    public Response sumDistanceTraveledPerCompetitor(String regattaName, String raceName) {
        return sumDistanceTraveledPerCompetitorQuery(SailingPredefinedQueries.QUERY_DISTANCE_TRAVELED_PER_COMPETITOR, regattaName, raceName);
    }

    public Response sumDistanceTraveledPerCompetitorAndLegType(String regattaName, String raceName) {
        return sumDistanceTraveledPerCompetitorQuery(SailingPredefinedQueries.QUERY_DISTANCE_TRAVELED_PER_COMPETITOR_LEGTYPE, regattaName, raceName);
    }

    private Response sumDistanceTraveledPerCompetitorQuery(String predefinedQueriesName, String regattaName) {
        return sumDistanceTraveledPerCompetitorQuery(predefinedQueriesName, regattaName, null);
    }
    
    private Response sumDistanceTraveledPerCompetitorQuery(String predefinedQueriesName, String regattaName, String raceName) {
        Response response;
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = getPredefinedQuery(predefinedQueriesName);
        
        if (queryDefinitionDTO == null) {
            response = getBadIdentifierErrorResponse(predefinedQueriesName);
        } else {
            HashMap<FunctionDTO, HashSet<? extends Serializable>> race_FilterSelection = new HashMap<>();

            FunctionDTO getRegattaName = new FunctionDTO(true, "getRegatta().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
            HashSet<Serializable> getRegattaName_FilterSelection = new HashSet<>();
            getRegattaName_FilterSelection.add(regattaName);
            race_FilterSelection.put(getRegattaName, getRegattaName_FilterSelection);

            if(raceName != null) {
                FunctionDTO getRaceName = new FunctionDTO(true, "getRace().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
                HashSet<Serializable> getRaceName_FilterSelection = new HashSet<>();
                getRaceName_FilterSelection.add(raceName);

                race_FilterSelection.put(getRaceName, getRaceName_FilterSelection);
            }
            
            queryDefinitionDTO.setFilterSelectionFor(queryDefinitionDTO.getDataRetrieverChainDefinition().getRetrieverLevel(2), race_FilterSelection);
            response = runQuery(queryDefinitionDTO, regattaName, raceName, distanceMetersExtractor, "m", 2);
        }
        return response;
    }

    public Response sumManeuversPerCompetitor(String regattaName) {
        return sumManeuversPerCompetitorQuery(SailingPredefinedQueries.QUERY_MANEUVERS_PER_COMPETITOR, regattaName);
    }

    public Response sumManeuversPerCompetitor(String regattaName, String raceName) {
        return sumManeuversPerCompetitorQuery(SailingPredefinedQueries.QUERY_MANEUVERS_PER_COMPETITOR, regattaName, raceName);
    }

    private Response sumManeuversPerCompetitorQuery(String predefinedQueriesName, String regattaName) {
        return sumManeuversPerCompetitorQuery(predefinedQueriesName, regattaName, null);
    }
    
    private Response sumManeuversPerCompetitorQuery(String predefinedQueriesName, String regattaName, String raceName) {
        Response response;
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = getPredefinedQuery(predefinedQueriesName);
        
        if (queryDefinitionDTO == null) {
            response = getBadIdentifierErrorResponse(predefinedQueriesName);
        } else {
            HashMap<FunctionDTO, HashSet<? extends Serializable>> race_FilterSelection = new HashMap<>();

            FunctionDTO getRegattaName = new FunctionDTO(true, "getRegatta().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
            HashSet<Serializable> getRegattaName_FilterSelection = new HashSet<>();
            getRegattaName_FilterSelection.add(regattaName);
            race_FilterSelection.put(getRegattaName, getRegattaName_FilterSelection);

            if (raceName != null) {
                FunctionDTO getRaceName = new FunctionDTO(true, "getRace().getName()", HasTrackedRaceContext.class.getName(), String.class.getName(), new ArrayList<String>(), "", 0);
                HashSet<Serializable> getRaceName_FilterSelection = new HashSet<>();
                getRaceName_FilterSelection.add(raceName);
                race_FilterSelection.put(getRaceName, getRaceName_FilterSelection);
            }
            
            queryDefinitionDTO.setFilterSelectionFor(queryDefinitionDTO.getDataRetrieverChainDefinition().getRetrieverLevel(2), race_FilterSelection);            
            response = runQuery(queryDefinitionDTO, regattaName, raceName, null, null, 0);
        }
        return response;
    }

    private ModifiableStatisticQueryDefinitionDTO getPredefinedQuery(String identifier) {
        DataMiningServer dataMiningServer = getDataMiningServer();
        PredefinedQueryIdentifier queryIdentifier = new PredefinedQueryIdentifier(identifier, "");
        return dataMiningServer.getPredefinedQueryDefinitionDTO(queryIdentifier);
    }
    
    private <ResultType> Response runQuery(ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO, String regattaName, String raceName,
                              Function<ResultType, Number> numberExtractor, String resultUnit, int resultPlaces) {
        Response response;
        DataMiningServer dataMiningServer = getDataMiningServer();
        
        StatisticQueryDefinition<?, ?, ?, ?> queryDefinition = dataMiningServer.getQueryDefinitionForDTO(queryDefinitionDTO);
        Query<?> query = queryDefinition == null ? null : dataMiningServer.createQuery(queryDefinition);
        if (query == null) {
            response = getBadQueryDefinitionErrorResponse(queryDefinitionDTO);
        } else {
            DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
            long requestTimepoint = System.currentTimeMillis();
            @SuppressWarnings("unchecked")
            QueryResult<ResultType> result = (QueryResult<ResultType>) dataMiningServer.runNewQueryAndAbortPreviousQueries(session, query);
            
            if (result == null || result.isEmpty()) {
                response = getNoDataFoundErrorResponse();
            } else {
                try {
                    JSONObject jsonResult = new JSONObject();
                    jsonResult.put("state", result.getState());
                    jsonResult.put("requestTimepoint", requestTimepoint);
                    jsonResult.put("calculationDuration-s", roundDouble(result.getCalculationTimeInSeconds(), 2));
                    jsonResult.put("description", result.getResultSignifier());
                    if (regattaName != null && !regattaName.isEmpty()) {
                        jsonResult.put("regatta", regattaName);
                    }
                    if (raceName != null && !raceName.isEmpty()) {
                        jsonResult.put("race", raceName);
                    }
                    jsonResult.put("resultUnit", resultUnit != null && !resultUnit.isEmpty() ? resultUnit : "None");
                    jsonResult.put("results", resultValuesToJSON(result, numberExtractor, resultPlaces));
                    response = Response.ok(jsonResult.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
                } catch (NotJsonSerializableException e) {
                    response = getNotSerializableErrorResponse(e.getNotSerializableClass());
                }
            }
        }
        return response;
    }

    private <ResultType> JSONArray resultValuesToJSON(QueryResult<ResultType> result, Function<ResultType, Number> numberExtractor,
                                                      int places) throws NotJsonSerializableException {
        if (!Number.class.isAssignableFrom(result.getResultType()) && numberExtractor == null) {
            throw new NotJsonSerializableException(result.getResultType());
        }
        
        // TODO see Bug 3334
        Map<GroupKey, ResultType> values = result.getResults();
        JSONArray jsonResultValues = new JSONArray();
        for (GroupKey groupKey : values.keySet()) {
            JSONObject jsonResultEntry = new JSONObject();
            jsonResultEntry.put("groupKey", groupKeyToJSON(groupKey));
            Number value = numberExtractor != null ? numberExtractor.apply(values.get(groupKey)) : (Number) values.get(groupKey);
            jsonResultEntry.put("value", roundDouble(value.doubleValue(), places));
            jsonResultValues.add(jsonResultEntry);
        }
        return jsonResultValues;
    }

    private JSONArray groupKeyToJSON(GroupKey groupKey) throws NotJsonSerializableException {
        // TODO Convert the concrete key values and not just the string representation
        JSONArray jsonResultEntryGroupKeys = new JSONArray();
        if (groupKey instanceof GenericGroupKey<?>) {
            jsonResultEntryGroupKeys.add(simpleGroupKeyToJSON(groupKey));
        } else if (groupKey instanceof CompoundGroupKey) {
            for (GroupKey key : groupKey.getKeys()) {
                jsonResultEntryGroupKeys.add(groupKeyToJSON(key));
            }
        } else {
            throw new NotJsonSerializableException(groupKey.getClass());
        }
        return jsonResultEntryGroupKeys;
    }

    private Object simpleGroupKeyToJSON(GroupKey groupKey) {
        return groupKey.asString();
    }
    
}
