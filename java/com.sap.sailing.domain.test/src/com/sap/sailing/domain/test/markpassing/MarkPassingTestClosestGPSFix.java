package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;


public class MarkPassingTestClosestGPSFix extends AbstractMarkPassingTestNew {

    public MarkPassingTestClosestGPSFix() throws MalformedURLException, URISyntaxException {

        super(new DetectorClosestGPSFix());

    }

  
    
    @Test
    public void compareMarkpasses() {


        super.compareMarkpasses();
        


    }

}