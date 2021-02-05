package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChargebeeConfigurationDTO implements IsSerializable {
    private String siteName;

    public ChargebeeConfigurationDTO() {
    }

    public ChargebeeConfigurationDTO(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
