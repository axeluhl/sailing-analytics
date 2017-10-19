package com.sap.sailing.server.pairinglist;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;

public interface PairingList {
   Iterable<Competitor> getCompetitors(int filght,int fleet);
   Regatta getRegatta();
}
