package com.sap.sailing.server.windimport;

import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.windimport.WindImport.WindSequence;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class TestWindImport {
    
    @Test
    public void testImport() throws Exception {
        List<Wind> windImport = WindImport.importWind(getClass().getResourceAsStream("Wind_2012Nov23_2.csv"));
        assertThat(windImport.size(), is(11042));
        Wind wind_0 = windImport.get(0);
    }

    @Test
    public void test2013Jun26_0() throws Exception {
        List<Wind> windImport = WindImport.importWind(getClass().getResourceAsStream("2013Jun26_0.csv"));
        int fiveMinutes_seconds = 5 * 60;
		List<WindSequence> windSequences = WindImport.splitWindSequences(windImport, fiveMinutes_seconds);
        assertThat(windSequences.size(), is(12));
    }

}
