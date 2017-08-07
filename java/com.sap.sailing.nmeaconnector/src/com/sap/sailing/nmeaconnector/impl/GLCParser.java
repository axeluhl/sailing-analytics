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

import com.sap.sailing.nmeaconnector.GLCSentence;

import net.sf.marineapi.nmea.parser.SentenceParser;
import net.sf.marineapi.nmea.sentence.TalkerId;

/**
 * GSV sentence parser.
 * 
 * @author Kimmo Tuukkanen
 * @author Axel Uhl
 */
public class GLCParser extends SentenceParser implements GLCSentence {

    // field indices, in case someone plans to use them:
//    private static final int GRI_MICROSECONDS_BY_10 = 0;
//    private static final int MASTER_TOA_MICROSECONDS = 1;
//    private static final int MASTER_TOA_SIGNAL_STATUS = 2;
//    private static final int TIME_DIFF_1_MICROSECONDS = 3;
//    private static final int TIME_DIFF_1_SIGNAL_STATUS = 4;
//    private static final int TIME_DIFF_2_MICROSECONDS = 5;
//    private static final int TIME_DIFF_2_SIGNAL_STATUS = 6;
//    private static final int TIME_DIFF_3_MICROSECONDS = 7;
//    private static final int TIME_DIFF_3_SIGNAL_STATUS = 8;
//    private static final int TIME_DIFF_4_MICROSECONDS = 9;
//    private static final int TIME_DIFF_4_SIGNAL_STATUS = 10;
//    private static final int TIME_DIFF_5_MICROSECONDS = 11;
//    private static final int TIME_DIFF_5_SIGNAL_STATUS = 12;

    /**
     * Constructor.
     * 
     * @param nmea
     *            GLC Sentence
     */
    public GLCParser(String nmea) {
        super(nmea, "GLC");
    }

    /**
     * Creates an GSV parser with empty sentence.
     * 
     * @param talker
     *            TalkerId to set
     */
    public GLCParser(TalkerId talker) {
        super(talker, "GLC", 19);
    }
}
