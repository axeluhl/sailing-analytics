package com.sap.sailing.windestimation.jaxrs.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.windestimation.integration.WindEstimationFactoryServiceImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationDataResourceTest {

    @Test
    @Ignore
    public void testImportingFromMockFile() throws NotEnoughDataHasBeenAddedException, ClassNotFoundException,
            IOException, ParseException, InterruptedException {
        WindEstimationFactoryServiceImpl windEstimationFactoryService = new WindEstimationFactoryServiceImpl();
        assertFalse(windEstimationFactoryService.isReady());
        final WindEstimationDataClientMock client = new WindEstimationDataClientMock(
                new File("resources/wind_estimation_data"), windEstimationFactoryService);
        client.updateWindEstimationModels();
        assertTrue(windEstimationFactoryService.isReady());
    }

}