package com.sap.sse.pairinglist.impl;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl<Flight,Group,Competitor> implements PairingListTemplateFactory<Flight,Group,Competitor>{
    PairingListTemplateImpl<Flight, Group, Competitor> pairingListTemplateImpl;
    
    public PairingListTemplateFactoryImpl() {
        
    }
    @Override
    public PairingListTemplate<Flight, Group, Competitor> createPairingListTemplate(
            PairingFrameProvider<Flight, Group, Competitor> pPFP) {
          if(pairingListTemplateImpl==null){
              pairingListTemplateImpl= new PairingListTemplateImpl<>(pPFP);
              return pairingListTemplateImpl;
          }
          return pairingListTemplateImpl;
    }
}
