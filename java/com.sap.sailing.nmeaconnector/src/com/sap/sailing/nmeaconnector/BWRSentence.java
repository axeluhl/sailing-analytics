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
 * BWR sentence. Bearing and Distance to Waypoint – Rhumb Line Latitude, N/S, Longitude, E/W, UTC, Status.
 * The format of the sentence is:<pre>
 * 
 *           1         2    3    4     5  6  7  8  9 10 11 12   13
 *           |         |    |    |     |  |  |  |  |  |  |  |   |
 * $--BWR,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x.x,T,x.x,M,x.x,N,c--c*hh
 * 1) Time (UTC)
 * 2) Waypoint Latitude
 * 3) N = North, S = South
 * 4) Waypoint Longitude
 * 5) E = East, W = West
 * 6) Bearing, True
 * 7) T = True
 * 8) Bearing, Magnetic
 * 9) M = Magnetic
 * 10) Nautical Miles
 * 11) N = Nautical Miles
 * 12) Waypoint ID
 * 13) Checksum
 * </pre>
 */
public interface BWRSentence extends Sentence {
}
