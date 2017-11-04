package com.sap.sailing.server.trackfiles.impl.doublefix;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;

/**
 * This class consolidates fixes in a sub-second range to one fix per second and computes the average of all values.
 * 
 */
public final class DownsamplerTo1HzProcessor implements DoubleFixProcessor {
    private final Logger LOG = Logger.getLogger(DoubleVectorFixImporter.class.getName());
    final private ArrayList<DoubleVectorFixData> fixesInTheCurrentSecond = new ArrayList<>();
    final private int nrOfColumsInTrack;
    final private DoubleFixProcessor delegateProcesssor;
    private long currentSecond = 0;
    private long currentOffsetInMs = 0;
    private long countSourceTtl = 0;
    private long countImportedTtl = 0;

    public DownsamplerTo1HzProcessor(int nrOfColumsInTrack, 
            DoubleFixProcessor consumerOfConsolidatedFixes) {
        this.nrOfColumsInTrack = nrOfColumsInTrack;        
        this.delegateProcesssor = consumerOfConsolidatedFixes;
    }

    @Override
    public void accept(DoubleVectorFixData fix) {
        if (fix == null)
            return;
        fix.correctTimepointBy(currentOffsetInMs);
        if (fix.getFixSecond() < currentSecond) {
            currentOffsetInMs = (currentSecond - fix.getFixSecond() + 1) * 1000;
            LOG.warning("Timepoint before last second, using offset of " + currentOffsetInMs + "ms from now on");
        }
        if (currentSecond != fix.getFixSecond()) {
            computeDownsampledFixForCurrentSecond();
            currentSecond = fix.getFixSecond();
        }
        countSourceTtl++;
        fixesInTheCurrentSecond.add(fix);
    }

    public void finish() {
        computeDownsampledFixForCurrentSecond();
        delegateProcesssor.finish();
        LOG.fine("Imported " + countImportedTtl + " fixes from " + countSourceTtl);
    }

    private void computeDownsampledFixForCurrentSecond() {
        final Double[] computedAverage = new Double[nrOfColumsInTrack];
        final int[] counts = new int[nrOfColumsInTrack];
        final int numberOfFixesInSecond = fixesInTheCurrentSecond.size();
        if (numberOfFixesInSecond == 0) {
            return;
        }
        for (DoubleVectorFixData d : fixesInTheCurrentSecond) {
            final Double[] fix = d.getFix();
            for (int colIdx = 0; colIdx < nrOfColumsInTrack; colIdx++) {
                if (fix[colIdx] != null) {
                    if (computedAverage[colIdx] == null) {
                        computedAverage[colIdx] = fix[colIdx];
                    } else {
                        computedAverage[colIdx] += fix[colIdx];
                    }
                    counts[colIdx]++;
                }
            }
        }
        fixesInTheCurrentSecond.clear();
        for (int colIdx = 0; colIdx < nrOfColumsInTrack; colIdx++) {
            if (computedAverage[colIdx] != null) {
                computedAverage[colIdx] /= (double) counts[colIdx];
            }
        }
        delegateProcesssor.accept(new DoubleVectorFixData(currentSecond * 1000 + 500, computedAverage));
        countImportedTtl++;
    }

    public long getCountImportedTtl() {
        return countImportedTtl;
    }

    public long getCountSourceTtl() {
        return countSourceTtl;
    }
}