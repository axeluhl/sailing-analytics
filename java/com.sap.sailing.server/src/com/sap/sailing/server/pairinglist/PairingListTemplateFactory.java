package com.sap.sailing.server.pairinglist;

public interface PairingListTemplateFactory {
    
    public PairingListTemplateFactory INSTANCE = new PairingListTemplateFactory();
    
    public PairingListTemplate createPairingListTemplate(int competitors, int flights, int boats);
    
}