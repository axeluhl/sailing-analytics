package com.sap.sailing.server.gateway.windimport.bravo;

import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet;

public class BravoWindImportServlet extends AbstractWindImportServlet {
    private static final long serialVersionUID = -4547876638456305135L;

    public BravoWindImportServlet() {
        super(new BravoWindImporter());
    }
}
