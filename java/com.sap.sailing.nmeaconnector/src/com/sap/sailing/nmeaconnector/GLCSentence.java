/* 
 * MWVSentence.java
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
package com.sap.sailing.nmeaconnector;

import net.sf.marineapi.nmea.sentence.Sentence;

/**
 * This sentence is obsolete over most of its former coverage area.
 * The US/Canadian/Russian Loran-C network was shut down in 2010; it is still as of 2015 in limited use in Europe.
 * Loran-C operations in Norway will cease from 1st Jan 2016. [NORWAY]
 * The format of the sentence is:<pre>
 *                                            12    14
 *         1    2   3 4   5 6   7 8   9 10  11|   13|
 *         |    |   | |   | |   | |   | |   | |   | |
 *  $--GLC,xxxx,x.x,a,x.x,a,x.x,a.x,x,a,x.x,a,x.x,a*hh<CR><LF>
 * Field Number:
 * GRI Microseconds/10
 * Master TOA Microseconds
 * Master TOA Signal Status
 * Time Difference 1 Microseconds
 * Time Difference 1 Signal Status
 * Time Difference 2 Microseconds
 * Time Difference 2 Signal Status
 * Time Difference 3 Microseconds
 * Time Difference 3 Signal Status
 * Time Difference 4 Microseconds
 * Time Difference 4 Signal Status
 * Time Difference 5 Microseconds
 * Time Difference 5 Signal Status
 * Checksum
 * </pre>
 */
public interface GLCSentence extends Sentence {
}
