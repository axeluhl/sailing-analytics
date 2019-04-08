package com.sap.sailing.server.gateway.windimport.expedition;

import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet;

public class WindImportServlet extends AbstractWindImportServlet {

    private static final long serialVersionUID = 1L;
    
    public WindImportServlet() {
        super(new WindImporter());
    }
}
