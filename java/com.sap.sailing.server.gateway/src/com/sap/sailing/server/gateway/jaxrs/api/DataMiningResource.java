package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.RestServletContainer;
import com.sap.sse.datamining.DataMiningServer;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.UUIDDataMiningSession;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

/**
 * REST-API to run predefined queries and get the result as JSON.
 * @author Lennart Hensler (D054527)
 */
@Path("/v1/datamining")
public class DataMiningResource extends AbstractSailingServerResource {
    @Context ServletContext servletContext;
    
    private ServiceTracker<DataMiningServer, DataMiningServer> dataMiningServerTracker;
    
    public DataMiningResource() {
        dataMiningServerTracker = null;
    }
    
    private DataMiningServer getDataMiningServer() {
        if (dataMiningServerTracker == null) {
            BundleContext context = (BundleContext) servletContext.getAttribute(RestServletContainer.OSGI_RFC66_WEBBUNDLE_BUNDLECONTEXT_NAME);
            dataMiningServerTracker = new ServiceTracker<DataMiningServer, DataMiningServer>(context, DataMiningServer.class.getName(), null);
            dataMiningServerTracker.open();
        }
        return dataMiningServerTracker.getService(); 
    }
    

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("query/identifiers")
    public Response getPredefinedQueryNames() {
        JSONArray predefinedQueryNames = new JSONArray();
        for (PredefinedQueryIdentifier id : getDataMiningServer().getPredefinedQueryIdentifiers()) {
            JSONObject jsonId = new JSONObject();
            jsonId.put("Identifier", id.getIdentifier());
            jsonId.put("Description", id.getDescription());
            predefinedQueryNames.add(jsonId);
        }
        
        String json = predefinedQueryNames.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("query/{identifier}")
    public Response runPredefinedQuery(@PathParam("identifier") String identifier) {
        Response response; 
        
        DataMiningServer dataMiningServer = getDataMiningServer();
        PredefinedQueryIdentifier queryIdentifier = new PredefinedQueryIdentifier(identifier, "");
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = dataMiningServer.getPredefinedQueryDefinitionDTO(queryIdentifier);
        if (queryDefinitionDTO == null) {
            response = Response.status(Status.NOT_FOUND).entity("Couldn't find a predefined query with the identifier '" + identifier + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            StatisticQueryDefinition<?, ?, ?, ?> queryDefinition = dataMiningServer.getQueryDefinitionForDTO(queryDefinitionDTO);
            Query<?> query = queryDefinition == null ? null : dataMiningServer.createQuery(queryDefinition);
            if (query == null) {
                response = Response.status(Status.NOT_FOUND).entity("Couldn't create a query for the identifier '" + identifier + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {
                DataMiningSession session = new UUIDDataMiningSession(UUID.randomUUID());
                QueryResult<?> result = dataMiningServer.runNewQueryAndAbortPreviousQueries(session, query);
                
                if (result == null || result.isEmpty()) {
                    response = Response.status(Status.NOT_FOUND).entity("No data found.").type(MediaType.TEXT_PLAIN).build();
                } else {
                    JSONObject jsonResult = new JSONObject();
                    jsonResult.put("state", result.getState());
                    jsonResult.put("signifier", result.getResultSignifier());
                    jsonResult.put("resultType", result.getResultType().getSimpleName());
                    jsonResult.put("results", resultValuesToJSON(result.getResultType(), result.getResults()));
                    response = Response.ok(jsonResult.toJSONString(), MediaType.APPLICATION_JSON).build();
                }
            }
        }
        return response;
    }
    
    @SuppressWarnings("unchecked")
    private JSONArray resultValuesToJSON(Class<?> resultType, Map<GroupKey, ?> resultValues) {
        if (Number.class.isAssignableFrom(resultType)) {
            return numericalResultValuesToJSON((Map<GroupKey, Number>) resultValues);
        }
        throw new UnsupportedOperationException("Can't convert values of type " + resultType.getName() + " to JSON");
    }
    
    private JSONArray numericalResultValuesToJSON(Map<GroupKey, Number> resultValues) {
        JSONArray jsonResultValues = new JSONArray();
        for (GroupKey groupKey : resultValues.keySet()) {
            JSONObject jsonResultEntry = new JSONObject();
            jsonResultEntry.put(groupKey.asString(), resultValues.get(groupKey));
            jsonResultValues.add(jsonResultEntry);
        }
        return jsonResultValues;
    }
    
}
