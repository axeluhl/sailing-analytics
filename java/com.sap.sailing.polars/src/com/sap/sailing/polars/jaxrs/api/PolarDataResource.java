package com.sap.sailing.polars.jaxrs.api;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.jaxrs.serialization.AngleAndSpeedRegressionSerializer;
import com.sap.sailing.polars.jaxrs.serialization.GroupKeySerializer;
import com.sap.sailing.polars.jaxrs.serialization.IncrementalAnyOrderLeastSquaresImplSerializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.serialization.impl.MapSerializer;
import com.sap.sse.datamining.shared.GroupKey;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {

    private MapSerializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedSerializer;
    private MapSerializer<GroupKey, AngleAndSpeedRegression> cubicSerializer;

    public PolarDataResource() {
        cubicSerializer = new MapSerializer<>("cubic_regression", new GroupKeySerializer(),
                new AngleAndSpeedRegressionSerializer());
        speedSerializer = new MapSerializer<>("speed_regression", new GroupKeySerializer(),
                new IncrementalAnyOrderLeastSquaresImplSerializer());
    }

    @GET
    @Path("cubic_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getCubicRegression() {
        Map<GroupKey, AngleAndSpeedRegression> regressions = getPolarDataServiceImpl().getPolarDataMiner()
                .getCubicRegressionPerCourseProcessor().getRegressions();

        return Response.ok(cubicSerializer.serialize(regressions).toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Path("speed_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getSpeedRegression() {
        Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> regressions = getPolarDataServiceImpl().getPolarDataMiner()
                .getSpeedRegressionPerAngleClusterProcessor().getRegressionsImpl();

        return Response.ok(speedSerializer.serialize(regressions).toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

}
