package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LegTimepointDTO extends NamedDTO implements IsSerializable {
    public Date firstPassingDate;

    public Date lastPassingDate;

    public LegTimepointDTO() {}

    public LegTimepointDTO(String name) {
        super(name);
    }
    
}
