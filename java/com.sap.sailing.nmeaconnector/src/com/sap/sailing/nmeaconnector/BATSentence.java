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
 * BAT sentence. Used for the proprietary NMEA extension of the SailTimer WindVane's battery indicator message.
 * The format of the sentence is:<pre>
 * 
 * $WIBAT,a,b*hh
 * 
 * 1) Wind Vane Battery (‘0’= Low, ‘1’= Good)
 * 2) Base Unit battery level (‘0’ = Low, ‘9’ = Good)
 * 3) Checksum
 * </pre>
 */
public interface BATSentence extends Sentence {
    public static enum WindVaneBatteryStatus {
        LOW(0), GOOD(1);
        
        private final int value;
        
        private WindVaneBatteryStatus(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    WindVaneBatteryStatus getWindVaneBatteryStatus();
    
    void setWindVaneBatteryStatus(WindVaneBatteryStatus status);
    
    /**
     * Battery status is reported as value ranve 0..9 where 0 means "Low" and 9 means "Good."
     */
    int getBaseUnitBatteryLevel();
    
    void setBaseUnitBatteryLevel(int level);
}
