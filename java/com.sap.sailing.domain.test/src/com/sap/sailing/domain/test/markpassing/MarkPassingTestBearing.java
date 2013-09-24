package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class MarkPassingTestBearing extends AbstractMarkPassingTestNew {

    public MarkPassingTestBearing() throws MalformedURLException, URISyntaxException {

        super(new DetectorClosestGPSFix());

    }

    @Test
    public void compareMarkpasses() {

        super.compareMarkpasses();

    }

}