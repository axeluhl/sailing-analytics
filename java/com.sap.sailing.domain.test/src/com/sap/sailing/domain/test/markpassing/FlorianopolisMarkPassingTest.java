package com.sap.sailing.domain.test.markpassing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

public class FlorianopolisMarkPassingTest extends AbstractMarkPassingTest {

    public FlorianopolisMarkPassingTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void testRace1() throws IOException, InterruptedException, URISyntaxException {
        testRace("1");
    }

    @Test
    public void testRace2() throws IOException, InterruptedException, URISyntaxException {
        testRace("2");
    }

    @Test
    public void testRace3() throws IOException, InterruptedException, URISyntaxException {
        testRace("3");
    }

    @Test
    public void testRace4() throws IOException, InterruptedException, URISyntaxException {
        testRace("4");
    }
    @Test
    public void testRace5() throws IOException, InterruptedException, URISyntaxException {
        testRace("5");
    }
    @Test
    public void testRace6() throws IOException, InterruptedException, URISyntaxException {
        testRace("6");
    }
    @Test
    public void testRace7() throws IOException, InterruptedException, URISyntaxException {
        testRace("7");
    }
    @Test
    public void testRace8() throws IOException, InterruptedException, URISyntaxException {
        testRace("8");
    }

    @Test
    public void testRace10() throws IOException, InterruptedException, URISyntaxException {
        testRace("10");
    }

    @Test
    public void testRace11() throws IOException, InterruptedException, URISyntaxException {
        testRace("11");
    }

    @Test
    public void testRace12() throws IOException, InterruptedException, URISyntaxException {
        testRace("12");
    }

    @Test
    public void testRace13() throws IOException, InterruptedException, URISyntaxException {
        testRace("13");
    }

    @Test
    public void testRace14() throws IOException, InterruptedException, URISyntaxException {
        testRace("14");
    }

    @Test
    public void testRace16() throws IOException, InterruptedException, URISyntaxException {
        testRace("16");
    }

    @Test
    public void testRace17() throws IOException, InterruptedException, URISyntaxException {
        testRace("17");
    }

    @Test
    public void testRace18() throws IOException, InterruptedException, URISyntaxException {
        testRace("18");
    }

    @Test
    public void testRace19() throws IOException, InterruptedException, URISyntaxException {
        testRace("19");
    }

    @Test
    public void testRace22() throws IOException, InterruptedException, URISyntaxException {
        testRace("22");
    }

    @Test
    public void testRace24() throws IOException, InterruptedException, URISyntaxException {
        testRace("24");
    }

    /*
     * Alinghi acting wierd
     * 
     * @Test public void testRace23() throws IOException, InterruptedException, URISyntaxException { testRace(23); }
     */

    /*
     * All mark data seems to be missing
     * 
     * @Test public void testRace9() throws IOException, InterruptedException, URISyntaxException { testRace(9); }
     * 
     * @Test public void testRace20() throws IOException, InterruptedException, URISyntaxException { testRace(20); }
     */

    /*
     * Missing static mark info leads to skipping of all finishes.
     * 
     * @Test public void testRace25() throws IOException, InterruptedException, URISyntaxException { testRace(25); }
     * 
     * @Test public void testRace21() throws IOException, InterruptedException, URISyntaxException { testRace(21); }
     * 
     * @Test public void testRace15() throws IOException, InterruptedException, URISyntaxException { testRace(15); }
     * 
     * @Test public void testRace26() throws IOException, InterruptedException, URISyntaxException { testRace(26); }
     */

    @Override
    protected String getExpectedEventName() {
        return "ESS Florianopolis 2013";
    }

    @Override
    protected String getFileName() {
        return "event_20131112_ESSFlorian-Race_";
    }
}
