package com.sap.sailing.server.pairinglist;

import com.sap.sailing.domain.base.Regatta;

public interface PairingListTemplate {
    public PairingList createPairingList(Regatta r);
    public double getQuality();
}
