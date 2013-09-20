package com.sap.sailing.domain.test.markpassing;

import org.junit.Test;

public class BearingMarkPassingTest extends AbstractMarkPassingTestNew {

    public BearingMarkPassingTest() {

        super(new BearingBasedDetector());

    }

    @Test
    public void compareMarkpasses() {

        super.compareMarkpasses();

    }

}