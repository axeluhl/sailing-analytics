package com.sap.sailing.windestimation.jaxrs.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.parser.ParseException;

import com.sap.sailing.windestimation.integration.ReplicableWindEstimationFactoryService;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

/**
 * This class is used to replicate wind estimation models from the remote server using Apache {@link HttpClient}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationDataClient {

    private static final Logger logger = Logger.getLogger(WindEstimationDataClient.class.getName());

    private static final String RESOURCE = "windestimation/api/windestimation_data";

    private final ReplicableWindEstimationFactoryService windEstimationFactoryService;
    private final String windEstimationDataSourceURL;
    private final Optional<String> windEstimationModelBearerToken;

    public WindEstimationDataClient(String windEstimationDataSourceURL,
            ReplicableWindEstimationFactoryService windEstimationFactoryService, Optional<String> windEstimationModelBearerToken) {
        this.windEstimationFactoryService = windEstimationFactoryService;
        this.windEstimationDataSourceURL = windEstimationDataSourceURL;
        this.windEstimationModelBearerToken = windEstimationModelBearerToken;
    }

    /**
     * This method is used to update {@link ModelStore} of wind estimation with data received from remote server.
     */
    public void updateWindEstimationModels()
            throws IOException, ParseException, InterruptedException, ClassNotFoundException {
        try {
            logger.log(Level.INFO,
                    "Loading model data of wind estimation from remote server " + windEstimationDataSourceURL);
            final InputStream inputStream = getContentFromResponse();
            windEstimationFactoryService.clearReplicaState();
            windEstimationFactoryService.initiallyFillFrom(inputStream);
            logger.log(Level.INFO, "Loading model data of wind estimation from remote server "
                    + windEstimationDataSourceURL + " succeeded");
        } catch (Exception e) {
            // Catching ClientProtocolException to indicate problems with HTTP protocol
            logger.log(Level.WARNING, "Failed to load model data of wind estimation from remote server "
                    + windEstimationDataSourceURL + ", " + e.getMessage());
            windEstimationFactoryService.clearReplicaState();
        }
    }

    private String getAPIString() {
        return windEstimationDataSourceURL + (windEstimationDataSourceURL.endsWith("/") ? "" : "/") + RESOURCE;
    }

    protected InputStream getContentFromResponse() throws IOException, ParseException {
        final HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes()).build();
        final HttpGet getProcessor = new HttpGet(getAPIString());
        windEstimationModelBearerToken.ifPresent(bearerToken -> getProcessor.setHeader("Authorization", "Bearer " + bearerToken));
        final HttpResponse processorResponse = client.execute(getProcessor);
        if (processorResponse.getStatusLine().getStatusCode() >= 300) {
            throw new IOException("Error trying to load wind estimation data from "+getAPIString()+": "
                    +processorResponse.getStatusLine().getReasonPhrase()+" ("+processorResponse.getStatusLine().getStatusCode()+")");
        }
        return processorResponse.getEntity().getContent();
    }

}
