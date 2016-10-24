package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor;
import com.sap.sailing.polars.mining.SpeedRegressionPerAngleClusterProcessor;

public class PolarDataClient {

    // MOCKING
    public static final String HOST = "http://127.0.0.1:8888/polars/api/polar_data";
    
    public static final String CUBIC_REGRESSION = "/cubic_regression";
    public static final String SPEED_REGRESSION = "/speed_regression";
    
    private PolarDataServiceImpl polarDataServiceImpl;
    
    public PolarDataClient(PolarDataServiceImpl polarDataServiceImpl) {
        this.polarDataServiceImpl = polarDataServiceImpl;
    }

    public CubicRegressionPerCourseProcessor getCubicRegressionProcessor()
            throws IllegalStateException, IOException, ParseException {
        CubicRegressionPerCourseProcessor processor = polarDataServiceImpl.getPolarDataMiner().getCubicRegressionPerCourseProcessor();
//        HttpClient client = new SystemDefaultHttpClient();
//        HttpGet getProcessor = new HttpGet(HOST + CUBIC_REGRESSION);
//        HttpResponse processorResponse = client.execute(getProcessor);
        
        return processor;
    }
    
    public SpeedRegressionPerAngleClusterProcessor getSpeedRegressionProcessor()
            throws IllegalStateException, IOException, ParseException {
        SpeedRegressionPerAngleClusterProcessor processor = polarDataServiceImpl.getPolarDataMiner().getSpeedRegressionPerAngleClusterProcessor();
//        HttpClient client = new SystemDefaultHttpClient();
//        HttpGet getProcessor = new HttpGet(HOST + SPEED_REGRESSION);
//        HttpResponse processorResponse = client.execute(getProcessor);
        
        return processor;
    }

}
