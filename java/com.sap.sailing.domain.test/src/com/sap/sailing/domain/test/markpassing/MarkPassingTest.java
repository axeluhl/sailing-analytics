package com.sap.sailing.domain.test.markpassing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;


public class MarkPassingTest extends AbstractMarkPassingTestNew {

    public MarkPassingTest() throws MalformedURLException, URISyntaxException {

        super(new CandidateFinder());

    }

  
    
    @Test
    public void compareMarkpasses() {


        super.compareMarkpasses();
        


    }

}