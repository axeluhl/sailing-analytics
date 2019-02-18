package com.sap.sailing.windestimation.jaxrs.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.simple.parser.ParseException;

import com.sap.sailing.windestimation.integration.ReplicableWindEstimationFactoryService;
import com.sap.sailing.windestimation.jaxrs.client.WindEstimationDataClient;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationDataClientMock extends WindEstimationDataClient {

    private final File file;

    public WindEstimationDataClientMock(File file,
            ReplicableWindEstimationFactoryService windEstimationFactoryService) {
        super(null, windEstimationFactoryService);
        this.file = file;
    }

    @Override
    protected InputStream getContentFromResponse() throws IOException, ParseException {
        return new FileInputStream(file);
    }

}
