package com.sap.sailing.server.gateway.expeditionimport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.Wind;

public class TestWindLogParser {
    
    private static final double DOUBLE_TOLERANCE = 1e-7;

	@Test
    public void testCompleteRow() throws Exception {
        List<Wind> windImport = WindLogParser.importWind(getClass().getResourceAsStream("CompleteRow.csv"));
        assertThat(windImport.size(), is(1));
        Wind wind = windImport.get(0);
        assertWind(wind, 11.2, 257.2, 54.430272, 10.172062);
    }

    @Test
    public void testCompleteRowPermutatedColumns() throws Exception {
        List<Wind> windImport = WindLogParser.importWind(getClass().getResourceAsStream("CompleteRowPermutatedColumns.csv"));
        assertThat(windImport.size(), is(1));
        Wind wind = windImport.get(0);
        assertWind(wind, 11.2, 257.2, 54.430272, 10.172062);
        assertThat(wind.getTimePoint().asMillis(), is(1372231373011L)); 
    }

	@Test
    public void testWindFirst() throws Exception {
        List<Wind> windImport = WindLogParser.importWind(getClass().getResourceAsStream("WindFirst.csv"));
        assertThat(windImport.size(), is(1));
        Wind wind = windImport.get(0);
        assertWind(wind, 11.2, 257.2, 54.430272, 10.172062);
    }

    @Test
    public void testPositionFirst() throws Exception {
        List<Wind> windImport = WindLogParser.importWind(getClass().getResourceAsStream("PositionFirst.csv"));
        assertThat(windImport.size(), is(1));
        Wind wind = windImport.get(0);
        assertWind(wind, 11.2, 257.2, 54.430272, 10.172062);
    }

	private void assertWind(Wind wind, double knots, double degrees, double lat, double lon) {
		assertThat(wind.getKnots(), closeTo(knots, DOUBLE_TOLERANCE));
		assertThat(wind.getFrom().getDegrees(), closeTo(degrees, DOUBLE_TOLERANCE));
		assertThat(wind.getPosition().getLatDeg(), closeTo(lat, DOUBLE_TOLERANCE));
		assertThat(wind.getPosition().getLngDeg(), closeTo(lon, DOUBLE_TOLERANCE));
	}

//    @Test
//    public void test2013Jun26_0() throws Exception {
//        List<Wind> windImport = WindLogParser.importWind(getClass().getResourceAsStream("2013Jun26_0.csv"));
//        assertThat(windImport.size(), is(85586));
//    }

}
