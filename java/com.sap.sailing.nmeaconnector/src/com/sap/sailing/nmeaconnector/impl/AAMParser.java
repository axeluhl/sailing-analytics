/* 
 * GSVParser.java
 * Copyright (C) 2010 Kimmo Tuukkanen
 * 
 * This file is part of Java Marine API.
 * <http://ktuukkan.github.io/marine-api/>
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

import com.sap.sailing.nmeaconnector.AAMSentence;

import net.sf.marineapi.nmea.parser.SentenceParser;
import net.sf.marineapi.nmea.sentence.TalkerId;

/**
 * GSV sentence parser.
 * 
 * @author Kimmo Tuukkanen
 * @author Axel Uhl
 */
public class AAMParser extends SentenceParser implements AAMSentence {

    // field indices, in case someone plans to use them:
//    private static final int STATUS_1 = 0;
//    private static final int STATUS_2 = 1;
//    private static final int ARRIVAL_CIRCLE_RADIUS = 2;
//    private static final int UNIT_OF_RADIUS = 3;
//    private static final int WAYPOINT_ID = 4;

    /**
     * Constructor.
     * 
     * @param nmea
     *            GLC Sentence
     */
    public AAMParser(String nmea) {
        super(nmea, "AAM");
    }

    /**
     * Creates an GSV parser with empty sentence.
     * 
     * @param talker
     *            TalkerId to set
     */
    public AAMParser(TalkerId talker) {
        super(talker, "AAM", 5);
    }
}
