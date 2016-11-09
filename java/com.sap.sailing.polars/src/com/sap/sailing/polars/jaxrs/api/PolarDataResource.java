package com.sap.sailing.polars.jaxrs.api;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.jaxrs.serialization.AngleAndSpeedRegressionSerializer;
import com.sap.sailing.polars.jaxrs.serialization.ClusterBoundarySerializer;
import com.sap.sailing.polars.jaxrs.serialization.ClusterSerializer;
import com.sap.sailing.polars.jaxrs.serialization.CompoundGroupKeySerializer;
import com.sap.sailing.polars.jaxrs.serialization.DegreeBearingSerializer;
import com.sap.sailing.polars.jaxrs.serialization.IncrementalAnyOrderLeastSquaresImplSerializer;
import com.sap.sailing.polars.jaxrs.serialization.LegTypeSerializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MapSerializer;
import com.sap.sse.datamining.shared.GroupKey;

@Path("/polar_data")
public class PolarDataResource extends AbstractPolarResource {

    public static final String FIELD_FIX_COUNT_PER_BOAT_CLASS = "fixCountPerBoatClass";
    public static final String FIELD_LONG = "long";
    public static final String FIELD_CLUSTER = "cluster";
    public static final String FIELD_BOAT_CLASS = "boatClass";
    public static final String FIELD_LEG_TYPE = "legType";
    public static final String FIELD_SPEED_REGRESSION = "speed_regressions";
    public static final String FIELD_CUBIC_REGRESSION = "cubic_regressions";

    private final MapSerializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedSerializer;
    private final MapSerializer<GroupKey, AngleAndSpeedRegression> cubicSerializer;
    private final MapSerializer<BoatClass, Long> fixCountPerBoatClassSerialzier;

    public PolarDataResource() {
        final LinkedHashMap<String, JsonSerializer<?>> cubicKeySerializers = new LinkedHashMap<>();
        cubicKeySerializers.put(FIELD_LEG_TYPE, new LegTypeSerializer());
        cubicKeySerializers.put(FIELD_BOAT_CLASS, new BoatClassJsonSerializer());
        cubicSerializer = new MapSerializer<>(
                new CompoundGroupKeySerializer(cubicKeySerializers),
                new AngleAndSpeedRegressionSerializer());
        final LinkedHashMap<String, JsonSerializer<?>> speedKeySerializers = new LinkedHashMap<>();
        speedKeySerializers.put(FIELD_BOAT_CLASS, new BoatClassJsonSerializer());
        speedKeySerializers.put(FIELD_CLUSTER, new ClusterSerializer<Bearing>(new ClusterBoundarySerializer<>(new DegreeBearingSerializer())));
        speedSerializer = new MapSerializer<>(
                new CompoundGroupKeySerializer(speedKeySerializers),
                new IncrementalAnyOrderLeastSquaresImplSerializer());
        fixCountPerBoatClassSerialzier = new MapSerializer<>(new BoatClassJsonSerializer(), new JsonSerializer<Long>() {
            @Override
            public JSONObject serialize(Long object) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(FIELD_LONG, object);
                return jsonObject;
            }
        });
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getRegressions() {
        Map<GroupKey, AngleAndSpeedRegression> cubicRegressions = getPolarDataServiceImpl()
                .getCubicRegressionsPerCourse();
        Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedRegressions = getPolarDataServiceImpl()
                .getSpeedRegressionsPerAngle();
        Map<BoatClass, Long> fixCountPerBoatClass = getPolarDataServiceImpl().getFixCointPerBoatClass();
        JSONObject regressions = new JSONObject();
        regressions.put(FIELD_CUBIC_REGRESSION, cubicSerializer.serialize(cubicRegressions));
        regressions.put(FIELD_SPEED_REGRESSION, speedSerializer.serialize(speedRegressions));
        regressions.put(FIELD_FIX_COUNT_PER_BOAT_CLASS, fixCountPerBoatClassSerialzier.serialize(fixCountPerBoatClass));
        return Response.ok(regressions.toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

}
