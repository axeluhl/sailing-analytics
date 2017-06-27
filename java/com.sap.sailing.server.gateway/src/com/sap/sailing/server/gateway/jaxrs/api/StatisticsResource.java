package com.sap.sailing.server.gateway.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsByYearJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsJsonSerializer;

@Path("/v1/statistics")
public class StatisticsResource extends AbstractSailingServerResource {

    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    private final StatisticsByYearJsonSerializer statisticsByYearJsonSerializer = new StatisticsByYearJsonSerializer(
            new StatisticsJsonSerializer());

    @GET
    @Produces(CONTENT_TYPE_JSON_UTF8)
    @Path("years")
    public Response getStatisticsByYear() {
        JSONObject json = statisticsByYearJsonSerializer.serialize(getService().getLocalStatisticsByYear());
        return getJsonResponse(json);
    }

    private Response getJsonResponse(JSONAware json) {
        return Response.ok(json.toJSONString()).header(HEADER_NAME_CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8).build();
    }
}
