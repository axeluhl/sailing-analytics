package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.jaxrs.api.PolarDataResource;
import com.sap.sailing.polars.jaxrs.deserialization.AngleAndSpeedRegressionDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.GroupKeyDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.IncrementalAnyOrderLeastSquaresImplDeserializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
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

    // MOCKING
    private static final String HOST = "http://127.0.0.1:8888/polars/api/polar_data";

    private final PolarDataServiceImpl polarDataServiceImpl;

    /**
     * Default constructor is missing because we need {@link PolarDataServiceImpl} to reach regressions
     * 
     * @param polarDataServiceImpl
     */
    public PolarDataClient(PolarDataServiceImpl polarDataServiceImpl) {
        this.polarDataServiceImpl = polarDataServiceImpl;
    }

    /**
     * This method is used to update {@link PolarDataMiner} regressions with data received from remote server. Before
     * the regression maps will be updated it may be cleaned
     * 
     * @throws IOException
     * @throws ParseException
     */
    public void fetchPolarDataRegressions() throws IOException, ParseException {
        try {
            logger.log(Level.INFO, "Loading polar regression data from remote server " + HOST);

            HttpClient client = new SystemDefaultHttpClient();
            HttpGet getProcessor = new HttpGet(HOST);
            HttpResponse processorResponse = client.execute(getProcessor);

            JSONObject jsonObject = getJsonFromResponse(processorResponse);
            
            MapDeserializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedDeserializer = 
                    new MapDeserializer<>(new GroupKeyDeserializer<>(), new IncrementalAnyOrderLeastSquaresImplDeserializer());
            MapDeserializer<GroupKey, AngleAndSpeedRegression> cubicDeserializer = 
                    new MapDeserializer<>(new GroupKeyDeserializer<>(), new AngleAndSpeedRegressionDeserializer());

            Map<GroupKey, AngleAndSpeedRegression> cubicRegression = cubicDeserializer
                    .deserialize((JSONArray) jsonObject.get(PolarDataResource.FIELD_CUBIC_REGRESSION));
            Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedRegression = speedDeserializer
                    .deserialize((JSONArray) jsonObject.get(PolarDataResource.FIELD_SPEED_REGRESSION));

            polarDataServiceImpl.updateRegressions(cubicRegression, speedRegression);

            logger.log(Level.INFO, "Loading polar regression data from remote server " + HOST + " succeeded");
        } catch (ClientProtocolException e) {
            // Catching ClientProtocolException to indicate problems with HTTP protocol
            logger.log(Level.INFO,
                    "Failed to load polar regression data from remote server " + HOST + ", " + e.getMessage());
        }
    }

    private JSONObject getJsonFromResponse(HttpResponse response) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        final Header encoding = response.getEntity().getContentEncoding();
        final InputStream content = response.getEntity().getContent();
        final JSONObject json;
        try (final Reader reader = encoding == null ? new InputStreamReader(content) : new InputStreamReader(content, encoding.getValue())) {
            json = (JSONObject) jsonParser.parse(reader);
        }

        return json;
    }

}
