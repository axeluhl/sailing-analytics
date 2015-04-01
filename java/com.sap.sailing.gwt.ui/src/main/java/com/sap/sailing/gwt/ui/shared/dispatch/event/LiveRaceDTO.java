package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LiveRaceDTO implements IsSerializable {

    private String name;

    private LiveRaceDTO() {
    }

    public LiveRaceDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
