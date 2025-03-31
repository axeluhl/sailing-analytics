package com.sap.sailing.landscape.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.dto.NamedDTO;

public class ReleaseDTO extends NamedDTO implements IsSerializable {
    private static final long serialVersionUID = 1270609943760458511L;
    private String baseName;
    private TimePoint creationDate;
    
    @Deprecated
    ReleaseDTO() {} // for GWT RPC serialization only
    
    public ReleaseDTO(String name, String baseName, TimePoint creationDate) {
        super(name);
        this.baseName = baseName;
        this.creationDate = creationDate;
    }

    public String getBaseName() {
        return baseName;
    }

    public TimePoint getCreationDate() {
        return creationDate;
    }
}
