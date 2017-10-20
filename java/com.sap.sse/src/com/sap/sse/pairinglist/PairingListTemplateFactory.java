package com.sap.sse.pairinglist;

import com.sap.sse.pairinglist.impl.PairingListTemplateFactoryImpl;


public interface PairingListTemplateFactory<Flight, Group, Competitor> {
    
    public static PairingListTemplateFactoryImpl INSTANCE = new PairingListTemplateFactoryImpl();
    
    public PairingListTemplate<Flight,Group,Competitor> createPairingListTemplate(PairingFrameProvider<Flight, Group, Competitor> pPFP);
}