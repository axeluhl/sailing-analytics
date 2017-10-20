package com.sap.sse.pairinglist;

public interface PairingListTemplate {
   PairingList<?,?,?> createPairingList(PairingFrameProvider<?, ?, ?> pPFP); 
   double getQualitiy();
}
