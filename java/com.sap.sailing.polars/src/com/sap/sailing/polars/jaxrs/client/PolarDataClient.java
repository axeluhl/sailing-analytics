package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.jaxrs.api.PolarDataResource;
import com.sap.sailing.polars.jaxrs.deserialization.AngleAndSpeedRegressionDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.ClusterBoundaryDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.ClusterDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.CompoundGroupKeyDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.DegreeBearingDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.IncrementalAnyOrderLeastSquaresImplDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.LegTypeDeserializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.mining.BearingComparator;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.MapDeserializer;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * This class is used to replicate polar data regressions calculation from the remote server using Apache
 * {@link HttpClient}
 * 
 * @author Oleg_Zheleznov
 *
 */
public class PolarDataClient {

    private static final Logger logger = Logger.getLogger(PolarDataClient.class.getName());

    private static final String RESOURCE = "polars/api/polar_data";

    private final PolarDataServiceImpl polarDataServiceImpl;
    private final String polarDataSourceURL;
    private final SharedDomainFactory domainFactory;
    
    /**
     * Default constructor is missing because we need {@link PolarDataServiceImpl} to reach regressions
     * @param polarDataSourceURL
     *            archive server URL string
     * @param polarDataServiceImpl
     *            {@link PolarDataServiceImpl} service to work with
     */
    public PolarDataClient(String polarDataSourceURL, PolarDataServiceImpl polarDataServiceImpl, SharedDomainFactory domainFactory) {
        this.polarDataServiceImpl = polarDataServiceImpl;
        this.polarDataSourceURL = polarDataSourceURL;
        this.domainFactory = domainFactory;
    }

    /**
     * This method is used to update {@link PolarDataMiner} regressions with data received from remote server. Before
     * the regression maps will be updated it may be cleaned
     * 
     * @throws IOException
     * @throws ParseException
     */
    public void updatePolarDataRegressions() throws IOException, ParseException {
        try {
            logger.log(Level.INFO, "Loading polar regression data from remote server " + polarDataSourceURL);
            JSONObject jsonObject = getJsonFromResponse();
            LinkedHashMap<String, JsonDeserializer<?>> speedDeserializers = new LinkedHashMap<>();
            final BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(domainFactory);
            speedDeserializers.put(PolarDataResource.FIELD_BOAT_CLASS, boatClassDeserializer);
            speedDeserializers.put(PolarDataResource.FIELD_CLUSTER, new ClusterDeserializer<Bearing>(new ClusterBoundaryDeserializer<>(new DegreeBearingDeserializer(), new BearingComparator())));
            MapDeserializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedDeserializer = new MapDeserializer<>(
                    new CompoundGroupKeyDeserializer(speedDeserializers), 
                    new IncrementalAnyOrderLeastSquaresImplDeserializer());
            LinkedHashMap<String, JsonDeserializer<?>> cubicDeserializers = new LinkedHashMap<>();
            cubicDeserializers.put(PolarDataResource.FIELD_LEG_TYPE, new LegTypeDeserializer());
            cubicDeserializers.put(PolarDataResource.FIELD_BOAT_CLASS, boatClassDeserializer);
            MapDeserializer<GroupKey, AngleAndSpeedRegression> cubicDeserializer = new MapDeserializer<>(
                    new CompoundGroupKeyDeserializer(cubicDeserializers), 
                    new AngleAndSpeedRegressionDeserializer());
            MapDeserializer<BoatClass, Long> fixCountPerBoatClassDeserializer = new MapDeserializer<>(
                    boatClassDeserializer, 
                    new JsonDeserializer<Long>() {
                        @Override
                        public Long deserialize(JSONObject object) throws JsonDeserializationException {
                            return (Long) object.get(PolarDataResource.FIELD_LONG);
                        }
                    });

            Map<GroupKey, AngleAndSpeedRegression> cubicRegression = cubicDeserializer
                    .deserialize((JSONArray) jsonObject.get(PolarDataResource.FIELD_CUBIC_REGRESSION));
            Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedRegression = speedDeserializer
                    .deserialize((JSONArray) jsonObject.get(PolarDataResource.FIELD_SPEED_REGRESSION));
            Map<BoatClass, Long> fixCountPerBoatClass = fixCountPerBoatClassDeserializer.deserialize((JSONArray) jsonObject.get(PolarDataResource.FIELD_FIX_COUNT_PER_BOAT_CLASS));

            polarDataServiceImpl.updateRegressions(cubicRegression, speedRegression);
            polarDataServiceImpl.updateFixCountPerBoatClass(fixCountPerBoatClass);

            logger.log(Level.INFO,
                    "Loading polar regression data from remote server " + polarDataSourceURL + " succeeded");
        } catch (ClientProtocolException e) {
            // Catching ClientProtocolException to indicate problems with HTTP protocol
            logger.log(Level.WARNING, "Failed to load polar regression data from remote server " + polarDataSourceURL
                    + ", " + e.getMessage());
        }
    }

    private String getAPIString() {
        if (polarDataSourceURL.endsWith("/")) {
            return polarDataSourceURL + RESOURCE;
        } else {
            return polarDataSourceURL + "/" + RESOURCE;
        }
    }

    protected JSONObject getJsonFromResponse() throws IOException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(getAPIString());
        HttpResponse processorResponse = client.execute(getProcessor);
        
        JSONParser jsonParser = new JSONParser();
        final Header encoding = processorResponse.getEntity().getContentEncoding();
        final InputStream content = processorResponse.getEntity().getContent();
        final JSONObject json;
        try (final Reader reader = encoding == null ? new InputStreamReader(content)
                : new InputStreamReader(content, encoding.getValue())) {
            json = (JSONObject) jsonParser.parse(reader);
        }

        return json;
    }

}
