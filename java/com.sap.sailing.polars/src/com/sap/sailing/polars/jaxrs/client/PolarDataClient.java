package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.polars.jaxrs.api.PolarDataResource;
import com.sap.sailing.polars.jaxrs.deserialization.GPSFixMovingWithPolarContextJsonDeserializer;
import com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class PolarDataClient {

    // MOCKING
    public static final String HOST = "http://127.0.0.1:8888/polars/api/polar_data";
    public static final String CUBIC_REGRESSION = "/cubic_regression";

    private GPSFixMovingWithPolarContextJsonDeserializer gpsFixMovingJsonDeserializer = new GPSFixMovingWithPolarContextJsonDeserializer();

    public CubicRegressionPerCourseProcessor getCubicRegressionProcessor()
            throws IllegalStateException, IOException, ParseException {
        CubicRegressionPerCourseProcessor processor = new CubicRegressionPerCourseProcessor();
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(HOST + CUBIC_REGRESSION);
        HttpResponse processorResponse = client.execute(getProcessor);

        JSONObject json = getJsonFromResponse(processorResponse);
        JSONArray dataEntries = (JSONArray) json.get(PolarDataResource.FIELD_REGRESSION);

        for (int i = 0; i < dataEntries.size(); i++) {
            JSONObject dataEntry = (JSONObject) dataEntries.get(i);

            GroupedDataEntry<GPSFixMovingWithPolarContext> entry = new GroupedDataEntry<>(
                    new GenericGroupKey<>(dataEntry.get(PolarDataResource.FIELD_KEY)),
                    gpsFixMovingJsonDeserializer.deserialize((JSONObject) dataEntry.get(PolarDataResource.FIELD_ENTRY)));

            processor.processElement(entry);
        }

        return processor;
    }

    private JSONObject getJsonFromResponse(HttpResponse response)
            throws IllegalStateException, IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        Header contentEncoding = response.getEntity().getContentEncoding();
        Reader reader;
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
