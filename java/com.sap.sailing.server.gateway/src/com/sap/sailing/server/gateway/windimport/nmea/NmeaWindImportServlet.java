package com.sap.sailing.server.gateway.windimport.nmea;

import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet;

public class NmeaWindImportServlet extends AbstractWindImportServlet {
    private static final long serialVersionUID = -4547876638456305135L;
    
    public NmeaWindImportServlet() {
        super(new NmeaWindImporter());
    }
}
