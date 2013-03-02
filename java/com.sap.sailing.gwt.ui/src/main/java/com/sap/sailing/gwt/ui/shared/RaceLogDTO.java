package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceLogDTO implements IsSerializable {
    public List<RaceLogEventDTO> raceEvents;

    // for GWT serialization
    public RaceLogDTO() {
        raceEvents = new ArrayList<RaceLogEventDTO>();
    }

}
