package com.sap.sailing.domain.tractracadapter.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class ClientParamsPHPArenal49erRace2Test extends AbstractClientParamsPHPTest {
    
    @Before
    public void setUp() throws IOException {
        setUp("/clientparamsArenal49erRace2.php");
    }
    
    @Test
    public void testEndOfTracking() {
        // 2012-03-16 12:39:00
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, 2012);
        calendar.set(Calendar.MONTH, 2);
        calendar.set(Calendar.DAY_OF_MONTH, 16);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 39);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar.getTime(), clientParams.getRaceTrackingEndTime().asDate());
    }
}
