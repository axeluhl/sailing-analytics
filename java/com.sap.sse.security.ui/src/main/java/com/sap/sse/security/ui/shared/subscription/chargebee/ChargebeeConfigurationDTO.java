package com.sap.sse.security.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChargebeeConfigurationDTO implements IsSerializable {
    private String siteName;

    @Deprecated
    ChargebeeConfigurationDTO() {} // for GWT serialization only

    public ChargebeeConfigurationDTO(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteName() {
        return siteName;
    }
}
