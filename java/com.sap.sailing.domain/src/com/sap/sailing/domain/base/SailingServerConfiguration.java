package com.sap.sailing.domain.base;

import java.io.Serializable;

/**
 * Represents the configuration of an (local) instance of a sailing server.
 * 
 * @author Frank
 * 
 */
public interface SailingServerConfiguration extends Serializable {
    boolean isStandaloneServer();

    void setStandaloneServer(boolean isStandaloneServer);
}
