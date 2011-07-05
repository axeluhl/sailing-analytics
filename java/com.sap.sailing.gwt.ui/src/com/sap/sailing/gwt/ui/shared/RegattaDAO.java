package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaDAO implements IsSerializable {
    public BoatClassDAO boatClass;
    public Iterable<RaceDAO> races;
}
