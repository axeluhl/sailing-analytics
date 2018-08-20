package com.sap.sse.datamining.ui.test;

import com.sap.sse.datamining.ui.test.client.TestSSEDataMiningUi;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author D064866
 *
 */
public class TestSSEDataMiningUiSuite extends GWTTestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for TestSSEDataMiningUi");
        suite.addTestSuite(TestSSEDataMiningUi.class);
        return suite;
    }
}
