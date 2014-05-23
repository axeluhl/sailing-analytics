package com.sap.sailing.gwt.ui.polarsheets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.EntryPointHelper;

public class PolarSheetsEntryPoint extends AbstractEntryPoint implements RegattaRefresher {

    private Set<RegattaDisplayer> regattaDisplayers;

    private final SailingServiceAsync sailingService = GWT.create(SailingService.class);

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        EntryPointHelper.registerASyncService((ServiceDefTarget) sailingService, RemoteServiceMappingConstants.sailingServiceRemotePath);

        regattaDisplayers = new HashSet<RegattaDisplayer>();
        createUI();
        fillRegattas();
    }

    private void createUI() {
        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        PolarSheetsPanel polarSheetsPanel = new PolarSheetsPanel(sailingService, this, stringMessages, this);
        polarSheetsPanel.addStyleName(PolarSheetsPanel.POLARSHEETS_STYLE);
        regattaDisplayers.add(polarSheetsPanel);
        rootPanel.add(polarSheetsPanel);
    }

    @Override
    public void fillRegattas() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onSuccess(List<RegattaDTO> result) {
                for (RegattaDisplayer regattaDisplayer : regattaDisplayers) {
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
