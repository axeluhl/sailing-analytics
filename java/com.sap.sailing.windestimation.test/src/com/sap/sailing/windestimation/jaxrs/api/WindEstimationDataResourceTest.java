package com.sap.sailing.windestimation.jaxrs.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.windestimation.integration.IncrementalMstHmmWindEstimationForTrackedRaceTest;
import com.sap.sailing.windestimation.integration.WindEstimationFactoryServiceImpl;
import com.sap.sailing.windestimation.model.store.ClassPathReadOnlyModelStoreImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationDataResourceTest {

    @Test
    public void testImportingFromMockFile() throws NotEnoughDataHasBeenAddedException, ClassNotFoundException,
            IOException, ParseException, InterruptedException {
        WindEstimationFactoryServiceImpl windEstimationFactoryService = new WindEstimationFactoryServiceImpl();
        assertFalse(windEstimationFactoryService.isReady());
        ClassPathReadOnlyModelStoreImpl modelStore = new ClassPathReadOnlyModelStoreImpl(
                "trained_wind_estimation_models", getClass().getClassLoader(),
                IncrementalMstHmmWindEstimationForTrackedRaceTest.modelFilesNames);
        final WindEstimationDataClientMock client = new WindEstimationDataClientMock(modelStore,
                windEstimationFactoryService);
        client.updateWindEstimationModels();
        assertTrue(windEstimationFactoryService.isReady());
    }

}