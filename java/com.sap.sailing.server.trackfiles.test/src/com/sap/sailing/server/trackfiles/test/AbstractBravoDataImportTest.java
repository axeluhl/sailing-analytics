package com.sap.sailing.server.trackfiles.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.BravoFixImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.BaseBravoDataImporterImpl;
import com.sap.sailing.server.trackfiles.impl.BravoDataImporterImpl;
import com.sap.sailing.server.trackfiles.impl.doublefix.DownsamplerTo1HzProcessor;
import com.sap.sailing.server.trackfiles.impl.doublefix.LearningBatchProcessor;

public abstract class AbstractBravoDataImportTest {
    
    private DownsamplerTo1HzProcessor downsampler;

    protected BaseBravoDataImporterImpl bravoDataImporter;
    private int callbackCallCount = 0;
    private double sumRideHeightInMeters = 0.0;
    
    protected abstract int getTrackColumnCount();
    
    protected abstract Map<String, Integer> getColumnData();
    
    protected interface ImportDataDefinition {
        InputStream getInputStream();
        int getExpectedFixesCount();
        int getExpectedFixesConsolidated();
    }
    
    @Before
    public void setUp() {
        this.callbackCallCount = 0;
        this.sumRideHeightInMeters = 0.0;
        bravoDataImporter = new BaseBravoDataImporterImpl(getColumnData(), BravoDataImporterImpl.BRAVO_TYPE) {
            protected com.sap.sailing.server.trackfiles.impl.doublefix.DoubleFixProcessor createDownsamplingProcessor(
                    DoubleVectorFixImporter.Callback callback,
                    TrackFileImportDeviceIdentifier trackIdentifier) {
                final LearningBatchProcessor batchProcessor = new LearningBatchProcessor(5000, 5000, callback, trackIdentifier);
                downsampler = new DownsamplerTo1HzProcessor(getTrackColumnCount(), batchProcessor);
                return downsampler;
            }
        };
    }
    
    protected void testImport(ImportDataDefinition importData) throws FormatNotSupportedException, IOException {
        try (final InputStream is = importData.getInputStream()) {
            bravoDataImporter.importFixes(is, (fixes, device) -> {
                for (DoubleVectorFix fix : fixes) {
                    callbackCallCount++;
                    sumRideHeightInMeters += new BravoFixImpl(fix).getRideHeight().getMeters();
                }
            }, "filename", "source", /* downsample */ true);
            Assert.assertEquals(importData.getExpectedFixesCount(), downsampler.getCountSourceTtl());
            Assert.assertEquals(importData.getExpectedFixesConsolidated(), downsampler.getCountImportedTtl());
            Assert.assertEquals(importData.getExpectedFixesConsolidated(), callbackCallCount);
        }
    }

}
