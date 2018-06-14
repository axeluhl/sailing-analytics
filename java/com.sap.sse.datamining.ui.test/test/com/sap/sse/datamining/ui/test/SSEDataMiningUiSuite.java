package com.sap.sse.datamining.ui.test;

import com.sap.sse.datamining.ui.test.client.SSEDataMiningUiTest;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SSEDataMiningUiSuite extends GWTTestSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite("Tests for SSEDataMiningUi");
    suite.addTestSuite(SSEDataMiningUiTest.class);
    return suite;
  }
}
