package com.sap.sse.gwt.shared;

import com.sap.sse.gwt.client.context.data.SapSailingContextDataJSO;
import com.sap.sse.gwt.client.context.impl.SapSailingContextDataFactoryImpl;

public class Branding {

    private static final Branding INSTANCE;
    
    static {
        INSTANCE = new Branding();
    }

    public static Branding getInstance() {
        return INSTANCE;
    }

    private boolean active = true;

    public Branding() {
        try {
        SapSailingContextDataJSO dataJso = new SapSailingContextDataFactoryImpl().getInstance();
        active = !dataJso.isDebrandingActive();
        } catch (Exception e) {
            //FIXME handle this
        }
    }

    /**
     * 
     * @return true when 
     */
    public boolean isActive() {
        return active;
    }
}
