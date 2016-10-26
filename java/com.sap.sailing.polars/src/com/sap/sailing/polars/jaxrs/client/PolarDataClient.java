package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.jaxrs.deserialization.AngleAndSpeedRegressionDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.GroupKeyDeserializer;
import com.sap.sailing.polars.jaxrs.deserialization.IncrementalAnyOrderLeastSquaresImplDeserializer;
import com.sap.sailing.polars.mining.AngleAndSpeedRegression;
import com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor;
import com.sap.sailing.polars.mining.SpeedRegressionPerAngleClusterProcessor;
import com.sap.sailing.polars.regression.impl.IncrementalAnyOrderLeastSquaresImpl;
import com.sap.sailing.server.gateway.deserialization.impl.MapDeserializer;
import com.sap.sse.datamining.shared.GroupKey;

/**
 * This class is used to replicate polar data regressions calculation from the remote server using Apache {@link HttpClient}
 * 
 * @author Oleg_Zheleznov
 *
 */
public class PolarDataClient {

    // MOCKING
    private static final String HOST = "http://127.0.0.1:8888/polars/api/polar_data/";

    private static final String CUBIC_REGRESSION = "cubic_regression";
    private static final String SPEED_REGRESSION = "speed_regression";

    private PolarDataServiceImpl polarDataServiceImpl;

    private MapDeserializer<GroupKey, IncrementalAnyOrderLeastSquaresImpl> speedDeserializer;
    private MapDeserializer<GroupKey, AngleAndSpeedRegression> cubicDeserializer;

    /**
     * Default constructor is missing because we need {@link PolarDataServiceImpl} to reach {@link PolarDataMiner} later
     * 
     * @param polarDataServiceImpl
     */
    public PolarDataClient(PolarDataServiceImpl polarDataServiceImpl) {
        this.polarDataServiceImpl = polarDataServiceImpl;

        speedDeserializer = new MapDeserializer<>(SPEED_REGRESSION, new GroupKeyDeserializer<>(),
                new IncrementalAnyOrderLeastSquaresImplDeserializer());
        cubicDeserializer = new MapDeserializer<>(CUBIC_REGRESSION, new GroupKeyDeserializer<>(),
                new AngleAndSpeedRegressionDeserializer());
    }

    /**
     * This method is used to update {@link CubicRegressionPerCourseProcessor} regressions with data received from remote server.
     * Before the regressions map will be updated it may be cleaned
     * 
     * @param clean determines whether regressions map will be cleaned or not
     * @return Updated {@link CubicRegressionPerCourseProcessor}
     * @throws IllegalStateException
     * @throws IOException
     * @throws ParseException
     */
    public CubicRegressionPerCourseProcessor getCubicRegressionProcessor(boolean clean)
            throws IllegalStateException, IOException, ParseException {
        CubicRegressionPerCourseProcessor processor = polarDataServiceImpl.getPolarDataMiner()
                .getCubicRegressionPerCourseProcessor();
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(HOST + CUBIC_REGRESSION);
        HttpResponse processorResponse = client.execute(getProcessor);

        Map<GroupKey, AngleAndSpeedRegression> map = cubicDeserializer
                .deserialize(getJsonFromResponse(processorResponse));

        if (clean) {
            processor.getRegressions().clear();
        }
        processor.getRegressions().putAll(map);

        return processor;
    }

    /**
     * This method is used to update {@link SpeedRegressionPerAngleClusterProcessor} regressions with data received from remote server.
     * Before the regressions map will be updated it may be cleaned
     * 
     * @param clean determines whether regressions map will be cleaned or not
     * @return Updated {@link SpeedRegressionPerAngleClusterProcessor}
     * @throws IllegalStateException
     * @throws IOException
     * @throws ParseException
     */
    public SpeedRegressionPerAngleClusterProcessor getSpeedRegressionProcessor(boolean clean)
            throws IllegalStateException, IOException, ParseException {
        SpeedRegressionPerAngleClusterProcessor processor = polarDataServiceImpl.getPolarDataMiner()
                .getSpeedRegressionPerAngleClusterProcessor();
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(HOST + SPEED_REGRESSION);
        HttpResponse processorResponse = client.execute(getProcessor);

        Map<GroupKey, IncrementalAnyOrderLeastSquaresImpl> map = speedDeserializer
                .deserialize(getJsonFromResponse(processorResponse));

        if (clean) {
            processor.getRegressions().clear();
        }
        processor.getRegressions().putAll(map);

        return processor;
    }

    private JSONObject getJsonFromResponse(HttpResponse response)
            throws IllegalStateException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        final Header contentEncoding = response.getEntity().getContentEncoding();
        final Reader reader;
        if (contentEncoding == null) {
            reader = new InputStreamReader(response.getEntity().getContent());
        } else {
            reader = new InputStreamReader(response.getEntity().getContent(), contentEncoding.getValue());
        }
        JSONObject json = (JSONObject) jsonParser.parse(reader);
        reader.close();
        return json;
    }

}
