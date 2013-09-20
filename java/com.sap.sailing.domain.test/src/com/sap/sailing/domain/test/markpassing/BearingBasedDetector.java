package com.sap.sailing.domain.test.markpassing;

public class BearingBasedDetector implements MarkPassingDetector {

    @Override
    public int computeMarkpasses(int gpsFixes, int courseAndMarks) {
        return gpsFixes + courseAndMarks;

    }

}
