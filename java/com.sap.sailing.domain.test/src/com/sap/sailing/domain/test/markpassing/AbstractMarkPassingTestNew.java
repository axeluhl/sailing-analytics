package com.sap.sailing.domain.test.markpassing;

import static org.junit.Assert.assertEquals;

public abstract class AbstractMarkPassingTestNew {
    private int gpsFixes = 5;
    private int courseAndMarks = 3;
    private int givenMarkPasses = 8;
   

    private final MarkPassingDetector detector;

    protected AbstractMarkPassingTestNew(MarkPassingDetector detector) {
        this.detector = detector;
    }

    protected void compareMarkpasses() {

        assertEquals("Detector failed", detector.computeMarkpasses(gpsFixes, courseAndMarks), givenMarkPasses);
    }
}
