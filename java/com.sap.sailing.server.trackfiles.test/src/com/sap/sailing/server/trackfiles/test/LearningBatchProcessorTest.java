package com.sap.sailing.server.trackfiles.test;

import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.server.trackfiles.impl.doublefix.DoubleVectorFixData;
import com.sap.sailing.server.trackfiles.impl.doublefix.LearningBatchProcessor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import org.junit.Assert;

public class LearningBatchProcessorTest {



    @Test
    public void testConsolidation() {
        
        MyCallback delegate = new MyCallback();
        LearningBatchProcessor processor = new LearningBatchProcessor(5, 10, delegate, device);
        int currentSecondAsMillis = 1 * 1000;
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 100, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 300, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 550, new Double[] { 0d, 0d, 0d, 0d }));
        Assert.assertEquals("Batch not full", 0, delegate.batchesDelivered);

        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 900, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 999, new Double[] { 0d, 0d, 0d, 0d }));
        Assert.assertEquals("Batch not full, should be learing", 0, delegate.batchesDelivered);

        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 130, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 200, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 560, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 600, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 130, new Double[] { 0d, 0d, 0d, 0d }));
        Assert.assertEquals("Batches after learning", 2, delegate.batchesDelivered);
        Assert.assertEquals("Last batch after learning", 5, delegate.lastBatchSize);

        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 200, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 560, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 561, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 562, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 563, new Double[] { 0d, 0d, 0d, 0d }));
        Assert.assertEquals("Next batch received", 3, delegate.batchesDelivered);
        Assert.assertEquals("First batch after learning", 5, delegate.lastBatchSize);

        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 564, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 600, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 700, new Double[] { 0d, 0d, 0d, 0d }));
        processor.accept(new DoubleVectorFixData(currentSecondAsMillis + 800, new Double[] { 0d, 0d, 0d, 0d }));

        Assert.assertEquals("Still on same batch", 3, delegate.batchesDelivered);
        processor.finish();
        
        Assert.assertEquals("Batch count after finish", 4, delegate.batchesDelivered);
        Assert.assertEquals("Last batch size after finish", 4, delegate.lastBatchSize);
        Assert.assertEquals("Last fix ", (currentSecondAsMillis + 800), delegate.lastFix.getTimePoint().asMillis());
    }
    


    private final class MyCallback implements DoubleVectorFixImporter.Callback {
        public int lastBatchSize = 0;
        public int batchesDelivered = 0;
        public DoubleVectorFix lastFix;
        
        @Override
        public void addFixes(Iterable<DoubleVectorFix> fix, TrackFileImportDeviceIdentifier device) {
            lastBatchSize = Util.size(fix);
            batchesDelivered++;
            lastFix = Util.last(fix);
        }
    }

    TrackFileImportDeviceIdentifier device = new TrackFileImportDeviceIdentifier() {
        private static final long serialVersionUID = 870061505508090216L;
        private UUID uuid = UUID.randomUUID();
        private MillisecondsTimePoint uploadedAt = new MillisecondsTimePoint(0);

        @Override
        public String getStringRepresentation() {
            return "stringRepresentation";
        }

        @Override
        public String getIdentifierType() {
            return "identifierType";
        }

        @Override
        public TimePoint getUploadedAt() {
            return uploadedAt;
        }

        @Override
        public String getTrackName() {
            return "trackname";
        }

        @Override
        public UUID getId() {
            return uuid;
        }

        @Override
        public String getFileName() {
            return "filename";
        }
    };
}
