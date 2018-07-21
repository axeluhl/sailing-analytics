package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsByYearJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsJsonSerializer;
import com.sap.sse.common.Util.Pair;

@Path("/v1/statistics")
public class StatisticsResource extends AbstractSailingServerResource {
    
    private static final Logger logger = Logger.getLogger(StatisticsResource.class.getName());

    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private final StatisticsByYearJsonSerializer statisticsByYearJsonSerializer = new StatisticsByYearJsonSerializer(
            new StatisticsJsonSerializer());

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("years")
    public Response getStatisticsByYear(@QueryParam("years") final List<String> years) {
        JSONArray json = new JSONArray();
        final Map<Integer, Statistics> statisticsByYear = getService().getLocalStatisticsByYear();
        final Predicate<Map.Entry<Integer, Statistics>> filterPredicate;
        if (years != null && !years.isEmpty()) {
            final Set<Integer> yearsToReturn = toIntegerYears(years);
            filterPredicate = entry -> yearsToReturn.contains(entry.getKey());
        } else {
            filterPredicate = entry -> true;
        }
        statisticsByYear.entrySet().stream().filter(filterPredicate).forEach(
                entry -> json.add(statisticsByYearJsonSerializer.serialize(new Pair<>(entry.getKey(), entry.getValue()))));
        return getJsonResponse(json);
    }

    private Set<Integer> toIntegerYears(final List<String> years) {
        final Set<Integer> integerYears = new HashSet<>();
        for (String yearString : years) {
            try {
                integerYears.add(Integer.valueOf(yearString.trim()));
            } catch (Exception e) {
                logger.log(Level.WARNING, e, () -> "error while parsing year '" + yearString + "'");
            }
        }
        return integerYears;
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
