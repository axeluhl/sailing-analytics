package com.sap.sailing.polars.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {
    
    @GET
    @Path("cubic_regression")
    @Produces("application/json;charset=UTF-8")
    public Response get() {
        JSONObject jsonDataMiner = new JSONObject();
        
        JSONArray cubicRegressionPerCourse = new JSONArray();
        for (GroupedDataEntry<GPSFixMovingWithPolarContext> entry: getPolarDataServiceImpl().getPolarDataMiner().getCubicRegressionPerCourseProcessor().getDataEntries()) {
            JSONObject jsonEntry = new JSONObject();
            
            GPSFixMovingWithPolarContextJsonSerializer serializer = 
                    new GPSFixMovingWithPolarContextJsonSerializer();
            
            jsonEntry.put("key", entry.getKey());
            jsonEntry.put("dataEntry", serializer.serialize(entry.getDataEntry()));
            
            cubicRegressionPerCourse.add(jsonEntry);
        }
        jsonDataMiner.put("cubicRegressionPerCourse", cubicRegressionPerCourse);
        
        String json = jsonDataMiner.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }
}
