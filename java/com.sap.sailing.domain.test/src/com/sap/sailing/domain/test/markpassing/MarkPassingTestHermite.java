package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class MarkPassingTestHermite extends AbstractMarkPassingTestNew {

    public MarkPassingTestHermite() throws MalformedURLException, URISyntaxException {
        super(new DetectorHermiteBased());

    }

    @Test
    public void compareMarkpasses() {

        super.compareMarkpasses();

    }

}