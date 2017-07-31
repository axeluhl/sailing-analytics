/* 
 * MWVParser.java
 * Copyright (C) 2011 Kimmo Tuukkanen
 * 
 * This file is part of Java Marine API.
 * <http://sourceforge.net/projects/marineapi/>
 * 
 * Java Marine API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Java Marine API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Marine API. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sap.sailing.nmeaconnector.impl;

import com.sap.sailing.nmeaconnector.BATSentence;

import net.sf.marineapi.nmea.parser.SentenceParser;
import net.sf.marineapi.nmea.sentence.TalkerId;

/**
 * BAT sentence parser. Used for the proprietary NMEA extension of the SailTimer WindVane's battery indicator message.
 * The format of the sentence is:<pre>
 * 
 * $WIBAT,a,b*hh
 * 
 * 1) Wind Vane Battery (‘0’= Low, ‘1’= Good)
 * 2) Base Unit battery level (‘0’ = Low, ‘9’ = Good)
 * 3) Checksum
 * </pre>
 * 
 * @author Axel Uhl
 */
public class BATParser extends SentenceParser implements BATSentence {
    private static final int WIND_VANE_BATTERY_STATUS = 0;
    private static final int BASE_UNIT_BATTERY_LEVEL = 1;

    /**
     * Creates a new instance of BATParser.
     * 
     * @param nmea BAT sentence String
     */
    public BATParser(String nmea) {
        super(nmea, /* Proprietary extension for SailTimer WindVane's battery indicator message */ "BAT");
    }

    /**
     * Creates a new empty instance of BATParser.
     * 
     * @param talker Talker id to set
     */
    public BATParser(TalkerId talker) {
        super(talker, /* Proprietary extension for SailTimer WindVane's battery indicator message */ "BAT", 2);
    }

    @Override
    public WindVaneBatteryStatus getWindVaneBatteryStatus() {
        WindVaneBatteryStatus result = null;
        final int intValue = getIntValue(WIND_VANE_BATTERY_STATUS);
        for (BATSentence.WindVaneBatteryStatus status : BATSentence.WindVaneBatteryStatus.values()) {
            if (status.getValue() == intValue) {
                result = status;
            }
        }
        return result;
    }

    @Override
    public int getBaseUnitBatteryLevel() {
        return getIntValue(BASE_UNIT_BATTERY_LEVEL);
    }

    @Override
    public void setWindVaneBatteryStatus(WindVaneBatteryStatus status) {
        setIntValue(WIND_VANE_BATTERY_STATUS, status.getValue());
    }

    @Override
    public void setBaseUnitBatteryLevel(int level) {
        if (level < 0 || level > 9) {
            throw new IllegalArgumentException("Level must be in the range 0..9 but was "+level);
        }
        setIntValue(BASE_UNIT_BATTERY_LEVEL, level);
    }
}
