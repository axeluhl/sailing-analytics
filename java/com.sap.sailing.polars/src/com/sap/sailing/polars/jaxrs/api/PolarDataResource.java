package com.sap.sailing.polars.jaxrs.api;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

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

    public static final String FIELD_SPEED_REGRESSION = "speed_regressions";
    public static final String FIELD_CUBIC_REGRESSION = "cubic_regressions";

    private final MapSerializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedSerializer;
    private final MapSerializer<GroupKey, AngleAndSpeedRegression> cubicSerializer;

    public PolarDataResource() {
        cubicSerializer = new MapSerializer<>(new GroupKeySerializer(), new AngleAndSpeedRegressionSerializer());
        speedSerializer = new MapSerializer<>(new GroupKeySerializer(),
                new IncrementalAnyOrderLeastSquaresImplSerializer());
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getRegressions() {
        Map<GroupKey, AngleAndSpeedRegression> cubicRegressions = getPolarDataServiceImpl()
                .getCubicRegressionsPerCourse();
        Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedRegressions = getPolarDataServiceImpl()
                .getSpeedRegressionsPerAngle();

        JSONObject regressions = new JSONObject();
        regressions.put(FIELD_CUBIC_REGRESSION, cubicSerializer.serialize(cubicRegressions));
        regressions.put(FIELD_SPEED_REGRESSION, speedSerializer.serialize(speedRegressions));

        return Response.ok(regressions.toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

}
