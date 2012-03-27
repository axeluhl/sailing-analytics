package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LegTimesInfoDTO extends NamedDTO implements IsSerializable {
    /**
     * The time point when the leg was first entered by a competitor; for the first leg the semantics
     * are slightly different: if the official race start time is known it is used instead of the first
     * passing event.
     */
    public Date firstPassingDate;

    public Date lastPassingDate;

    public LegTimesInfoDTO() {}

    public LegTimesInfoDTO(String name) {
        super(name);
    }
    
}
