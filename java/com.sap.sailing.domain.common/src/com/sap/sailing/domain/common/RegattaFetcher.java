package com.sap.sailing.domain.common;

public interface RegattaFetcher {
    /**
     * Not to be executed on the client; when executed on the server, returns an object of type <code>Regatta</code>
     */
    Object getRegatta(RegattaName regattaIdentifier);

}
