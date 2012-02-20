package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LegTimesInfoDTO extends NamedDTO implements IsSerializable {
    public Date firstPassingDate;

    public LegTimesInfoDTO() {}

    public LegTimesInfoDTO(String name) {
        super(name);
    }
    
}
