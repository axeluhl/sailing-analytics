package com.sap.sailing.landscape.common;

import java.io.Serializable;

/**
 * @author T Stokes
 */
public enum AzFormat implements Serializable {
    /**
     * A mixture of az id and name, in the form &lt;name&gt;/&lt;id&gt;
     */
    MIXED,

    /**
     * Just the az name
     */
    NAME,

    /**
     * Just the az id
     */
    ID;
}
