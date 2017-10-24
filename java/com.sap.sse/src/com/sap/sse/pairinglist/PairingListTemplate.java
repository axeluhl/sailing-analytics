package com.sap.sse.pairinglist;

public interface PairingListTemplate<Flight,Group,Competitor> {
   PairingList<Flight,Group,Competitor> createPairingList(PairingFrameProvider<Flight, Group, Competitor> pPFP); 
   
   double getQualitiy();
}