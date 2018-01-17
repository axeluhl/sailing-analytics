package com.sap.sailing.server.gateway.windimport.grib;

import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet;

public class GribWindImportServlet extends AbstractWindImportServlet {
    private static final long serialVersionUID = -4547876638456305135L;
    
    public GribWindImportServlet() {
        super(new GribWindImporter());
    }
}
