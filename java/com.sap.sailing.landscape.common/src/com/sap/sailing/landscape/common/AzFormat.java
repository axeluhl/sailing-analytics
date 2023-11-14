package com.sap.sailing.landscape.common;

import java.io.Serializable;

/**
 * MIXED A mixture of az id and name
 * NAME Just the az name
 * ID Just the az id
 * @author T Stokes
 *
 */
public enum AzFormat implements Serializable{
        MIXED,
        NAME,
        ID; 
}
