package com.sap.sailing.domain.base;

import java.io.Serializable;

public interface WithID {

    /**
     * Something that uniquely identifies this object beyond his name
     */
    Serializable getId();

}
