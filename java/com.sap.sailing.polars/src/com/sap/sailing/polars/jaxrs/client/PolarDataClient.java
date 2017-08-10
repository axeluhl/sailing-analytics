package com.sap.sailing.polars.jaxrs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.json.simple.parser.ParseException;

import com.sap.sailing.polars.ReplicablePolarService;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.mining.PolarDataMiner;

/**
 * This class is used to replicate polar data regressions calculation from the remote server using Apache
 * {@link HttpClient}
 * 
 * @author Oleg_Zheleznov
 * @author Axel Uhl (d043530)
 *
 */
public class PolarDataClient {

    private static final Logger logger = Logger.getLogger(PolarDataClient.class.getName());

    private static final String RESOURCE = "polars/api/polar_data";

    private final ReplicablePolarService polarDataService;
    private final String polarDataSourceURL;
    
    /**
     * Default constructor is missing because we need {@link PolarDataServiceImpl} to reach regressions
     * @param polarDataSourceURL
     *            archive server URL string
     * @param polarDataService
     *            {@link PolarDataServiceImpl} service to work with
     */
    public PolarDataClient(String polarDataSourceURL, ReplicablePolarService polarDataService) {
        this.polarDataService = polarDataService;
        this.polarDataSourceURL = polarDataSourceURL;
    }

    /**
     * This method is used to update {@link PolarDataMiner} regressions with data received from remote server. Before
     * the regression maps will be updated it may be cleaned
     */
    public void updatePolarDataRegressions() throws IOException, ParseException, InterruptedException, ClassNotFoundException {
        try {
            logger.log(Level.INFO, "Loading polar regression data from remote server " + polarDataSourceURL);
            final InputStream inputStream = getContentFromResponse();
            polarDataService.clearReplicaState();
            polarDataService.initiallyFillFrom(inputStream);
            logger.log(Level.INFO,
                    "Loading polar regression data from remote server " + polarDataSourceURL + " succeeded");
        } catch (Exception e) {
            // Catching ClientProtocolException to indicate problems with HTTP protocol
            logger.log(Level.WARNING, "Failed to load polar regression data from remote server " + polarDataSourceURL
                    + ", " + e.getMessage()+"; resetting polar data miner");
            polarDataService.resetState();
        }
    }

    private String getAPIString() {
        return polarDataSourceURL + (polarDataSourceURL.endsWith("/") ? "" : "/") + RESOURCE;
    }

    protected InputStream getContentFromResponse() throws IOException, ParseException {
        HttpClient client = new SystemDefaultHttpClient();
        HttpGet getProcessor = new HttpGet(getAPIString());
        HttpResponse processorResponse = client.execute(getProcessor);
        return processorResponse.getEntity().getContent();
    }

}
