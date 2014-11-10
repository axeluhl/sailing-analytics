package com.sap.sailing.gwt.ui.polarsheets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.EntryPointHelper;

public class PolarSheetsEntryPoint extends AbstractSailingEntryPoint implements RegattaRefresher {

    private Set<RegattasDisplayer> regattaDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);

        regattaDisplayers = new HashSet<RegattasDisplayer>();
        createUI();
        fillRegattas();
    }

    private void createUI() {
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        PolarSheetsPanel polarSheetsPanel = new PolarSheetsPanel(sailingService, this, getStringMessages(), this);
        polarSheetsPanel.addStyleName(PolarSheetsPanel.POLARSHEETS_STYLE);
        regattaDisplayers.add(polarSheetsPanel);
        rootPanel.add(polarSheetsPanel);
    }

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onSuccess(List<RegattaDTO> result) {
                for (RegattasDisplayer regattaDisplayer : regattaDisplayers) {
                    regattaDisplayer.fillRegattas(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                reportError("Remote Procedure Call getRegattas() - Failure");
            }
        });
    }

}
