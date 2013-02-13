package com.sap.sailing.gwt.ui.polarsheets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PolarSheetsEntryPoint extends AbstractEntryPoint implements RegattaRefresher {

    // private static final Logger logger =
    // Logger.getLogger(PolarSheetsEntryPoint.class.getName());

    private Set<RegattaDisplayer> regattaDisplayers;

    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        regattaDisplayers = new HashSet<RegattaDisplayer>();
        createUI();
        fillRegattas();
    }

    private void createUI() {
        RootPanel rootPanel = RootPanel.get();
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        ScrollPanel contentScrollPanel = new ScrollPanel();
        PolarSheetsPanel polarSheetsPanel = new PolarSheetsPanel(sailingService, this, stringMessages, this);
        polarSheetsPanel.addStyleName(PolarSheetsPanel.POLARSHEETS_STYLE);
        regattaDisplayers.add(polarSheetsPanel);
        contentScrollPanel.setWidget(polarSheetsPanel);
        mainPanel.add(contentScrollPanel);
        rootPanel.add(mainPanel);
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
