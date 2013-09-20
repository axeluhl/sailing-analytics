package com.sap.sailing.domain.test.markpassing;

import org.junit.Test;

public class HermiteMarkPassingTest extends AbstractMarkPassingTestNew {

    public HermiteMarkPassingTest() {
        super(new HermiteSplineBasedDetector());

    }

    @Test
    public void compareMarkpasses() {

        super.compareMarkpasses();

    }

}