package net.sf.marineapi.nmea.parser.test;

import static org.junit.Assert.assertEquals;
import net.sf.marineapi.nmea.parser.BATParser;
import net.sf.marineapi.nmea.sentence.BATSentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.BATSentence.WindVaneBatteryStatus;

import org.junit.Before;
import org.junit.Test;

public class BATTest {

    public static final String EXAMPLE = "$WIBAT,1,5";
    BATSentence empty;
    BATSentence bat;

    @Before
    public void setUp() throws Exception {
        empty = new BATParser(TalkerId.WI);
        bat = new BATParser(EXAMPLE);
    }

    @Test
    public void testBATParser() {
        assertEquals(TalkerId.WI, empty.getTalkerId());
        assertEquals("BAT", empty.getSentenceId());
        assertEquals(2, empty.getFieldCount());

    }

    @Test
    public void testBATParserString() {
        assertEquals(TalkerId.WI, bat.getTalkerId());
        assertEquals("BAT", bat.getSentenceId());
        assertEquals(2, bat.getFieldCount());
    }

    @Test
    public void testGetBaseUnitBatteryLevel() {
        assertEquals(5, bat.getBaseUnitBatteryLevel());
    }

    @Test
    public void testWindVaneBatteryStatus() {
        assertEquals(WindVaneBatteryStatus.GOOD, bat.getWindVaneBatteryStatus());
    }
}
