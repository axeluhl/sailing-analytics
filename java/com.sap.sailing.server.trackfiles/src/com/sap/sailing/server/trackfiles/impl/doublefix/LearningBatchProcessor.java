package com.sap.sailing.server.trackfiles.impl.doublefix;

import java.util.ArrayList;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.BaseDoubleVectorFixImporter.Callback;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public final class LearningBatchProcessor implements DoubleFixProcessor {
    private final int batchSize;
    private final ArrayList<DoubleVectorFixData> learnedFixes;
    private final ArrayList<DoubleVectorFix> collectedFixes;
    private final Callback callback;
    private final TrackFileImportDeviceIdentifier deviceIdentifier;
    private boolean isLearning = true;
    private long fixesToLearn;

    public LearningBatchProcessor(int batchSize, int fixesToLearn, Callback callback,
            TrackFileImportDeviceIdentifier deviceIdentifier) {
        super();
        this.batchSize = batchSize;
        this.fixesToLearn = fixesToLearn;
        this.collectedFixes = new ArrayList<>(batchSize);
        this.learnedFixes = new ArrayList<>(fixesToLearn);
        this.callback = callback;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public void accept(DoubleVectorFixData fix) {
        if (isLearning) {
            learnFix(fix);
        } else {
            processFix(fix);
        }
    }

    /**
     * In the learn phase the fixes just get collected for learning.
     * 
     * @param fix
     */
    private void learnFix(DoubleVectorFixData fix) {
        learnedFixes.add(fix);

        if (learnedFixes.size() >= fixesToLearn) {
            finishLearning();
        }
    }

    private void finishLearning() {
        // Now we have enough data to process
        isLearning = false;
        // process the loaded learning fixes
        for (DoubleVectorFixData doubleVectorFix : learnedFixes) {
            processFix(doubleVectorFix);
        }
    }

    /**
     * When processing a fix, data can be corrected before added to the collection of Fixes that will get stored in the
     * mongo....
     * 
     * @param fix
     */
    private void processFix(DoubleVectorFixData fix) {
        // TODO process fix
        collectedFixes.add(new DoubleVectorFixImpl(new MillisecondsTimePoint(fix.getTimepointInMs()), fix.getFix()));
        int currentlyCollectedFixes = collectedFixes.size();
        if (currentlyCollectedFixes >= batchSize) {
            pushCollectedFixes();
        }
      
    }

    @Override
    public void finish() {
        if (isLearning) {
            finishLearning();
        }
        pushCollectedFixes();
    }

    private void pushCollectedFixes() {
        callback.addFixes(collectedFixes, deviceIdentifier);
        collectedFixes.clear();
    }
}