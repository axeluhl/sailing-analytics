package com.sap.sailing.polars.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.jaxrs.serialization.ClusterGroupJsonSerializer;
import com.sap.sailing.polars.jaxrs.serialization.GPSFixMovingWithPolarContextJsonSerializer;
import com.sap.sailing.polars.jaxrs.serialization.PolarSheetGenerationSettingsJsonSerializer;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {

    @GET
    @Path("cubic_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getCubicRegression() {
        JSONObject jsonCubicRegression = new JSONObject();
        JSONArray cubicRegressionPerCourse = new JSONArray();
        for (GroupedDataEntry<GPSFixMovingWithPolarContext> entry : getPolarDataServiceImpl().getPolarDataMiner()
                .getCubicRegressionPerCourseProcessor().getDataEntries()) {
            JSONObject jsonEntry = new JSONObject();
            GPSFixMovingWithPolarContextJsonSerializer serializer = new GPSFixMovingWithPolarContextJsonSerializer();
            jsonEntry.put("key", entry.getKey());
            jsonEntry.put("dataEntry", serializer.serialize(entry.getDataEntry()));
            cubicRegressionPerCourse.add(jsonEntry);
        }
        jsonCubicRegression.put("cubicRegressionPerCourse", cubicRegressionPerCourse);
        String json = jsonCubicRegression.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Path("speed_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getSpeedRegression() {
        JSONObject jsonSpeedRegression = new JSONObject();
        jsonSpeedRegression.put("angleClusterGroup",
                new ClusterGroupJsonSerializer<Bearing>().serialize(getPolarDataServiceImpl().getPolarDataMiner()
                        .getSpeedRegressionPerAngleClusterProcessor().getAngleCluster()));
        String json = jsonSpeedRegression.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
    
    @GET
    @Path("angle_cluster_group")
    @Produces("application/json;charset=UTF-8")
    public Response getAngleClusterGroup() {
        String json = new ClusterGroupJsonSerializer<Bearing>().serialize(getPolarDataServiceImpl().getPolarDataMiner().getAngleClusterGroup()).toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
    
    @GET
    @Path("backend_polar_settings")
    @Produces("application/json;charset=UTF-8")
    public Response getBackendPolarSettings() {
        String json = new PolarSheetGenerationSettingsJsonSerializer().serialize(getPolarDataServiceImpl().getPolarDataMiner().getPolarSheetGenerationSettings()).toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}
