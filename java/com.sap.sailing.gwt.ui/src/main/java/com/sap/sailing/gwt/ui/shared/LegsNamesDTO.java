package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LegsNamesDTO implements IsSerializable {
    public String notificationMessage = "";
    public List<String> legsNames = null;

    public LegsNamesDTO() {
        this.notificationMessage = "";
        this.legsNames = new ArrayList<String>();
    }

    public LegsNamesDTO(String notificationMessage, List<String> legsNames) {
        this.notificationMessage = notificationMessage;
        this.legsNames = legsNames;
    }
}
