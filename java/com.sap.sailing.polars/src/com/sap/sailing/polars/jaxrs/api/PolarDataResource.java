package com.sap.sailing.polars.jaxrs.api;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.jaxrs.serialization.AngleAndSpeedRegressionSerializer;
import com.sap.sailing.polars.jaxrs.serialization.IncrementalAnyOrderLeastSquaresImplSerializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.serialization.impl.MapEntrySerializer;
import com.sap.sse.datamining.shared.GroupKey;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {

    private MapEntrySerializer<GroupKey, AngleAndSpeedRegression> cubicSerializer;
    private MapEntrySerializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedSerializer;
    private PolarDataMiner dataMiner;

    public PolarDataResource() {
        cubicSerializer = new MapEntrySerializer<>();
        speedSerializer = new MapEntrySerializer<>();

        cubicSerializer.setValueSerializer(new AngleAndSpeedRegressionSerializer());
        speedSerializer.setValueSerializer(new IncrementalAnyOrderLeastSquaresImplSerializer());

        dataMiner = getPolarDataServiceImpl().getPolarDataMiner();
    }

    @GET
    @Path("cubic_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getCubicRegression() {
        JSONArray regressionsJSON = new JSONArray();
        Map<GroupKey, AngleAndSpeedRegression> regressions = dataMiner.getCubicRegressionPerCourseProcessor()
                .getRegressions();

        regressions.entrySet().stream().forEach(entry -> regressionsJSON.add(cubicSerializer.serialize(entry)));

        return Response.ok(regressionsJSON.toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Path("speed_regression")
    @Produces("application/json;charset=UTF-8")
    public Response getSpeedRegression() {
        JSONArray regressionsJSON = new JSONArray();
        Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> regressions = dataMiner
                .getSpeedRegressionPerAngleClusterProcessor().getRegressionsImpl();

        regressions.entrySet().stream().forEach(entry -> regressionsJSON.add(speedSerializer.serialize(entry)));

        return Response.ok(regressionsJSON.toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

}
