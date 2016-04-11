package com.sap.sailing.server.trackfiles.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.BravoDataImporterImpl;

public class BravoDataImportTest {
    
    private final DoubleVectorFixImporter bravoDataImporter = new BravoDataImporterImpl();
    private int callbackCallCount = 0;
    private double sumRideHeight = 0.0;
    
    @Before
    public void setUp() {
        this.callbackCallCount = 0;
        this.sumRideHeight = 0.0;
    }
    
    @Test
    public void testImport() throws FormatNotSupportedException, IOException {
        ImportFiles fileToImport = ImportFiles.MUSCAT_RACE13_BRAVO;
        InputStream in = getClass().getResourceAsStream(fileToImport.fileName);
        bravoDataImporter.importFixes(in, (fix, device) -> {
            callbackCallCount++;
            sumRideHeight += new BravoFixImpl(fix).getRideHeight();
        }, "source");
        Assert.assertEquals(fileToImport.expectedFixesCount, callbackCallCount);
        Assert.assertEquals(fileToImport.expectedAverageRideHeight, sumRideHeight / callbackCallCount, 0.00001);
    }
    
    private enum ImportFiles {
        MUSCAT_RACE13_BRAVO("/Muscat Race13 - BRAVO.txt", 12828, 0.62372);
        
        private final String fileName;
        private final int expectedFixesCount;
        private final double expectedAverageRideHeight;

        private ImportFiles(String fileName, int expectedFixesCount, double expectedAverageRideHeight) {
            this.fileName = fileName;
            this.expectedFixesCount = expectedFixesCount;
            this.expectedAverageRideHeight = expectedAverageRideHeight;
        }
    }

}
