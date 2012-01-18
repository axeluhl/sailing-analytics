package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.adminconsole.RaceMap;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceBoardPanel extends FormPanel implements Component<RaceBoardSettings> {

    private final SailingServiceAsync sailingService;

    private final ErrorReporter errorReporter;

    // TODO Frank to use these later
//    private final StringMessages stringMessages;

    private String raceBoardName;

    private static RaceBoardResources resources = GWT.create(RaceBoardResources.class);

    // TODO Frank to use these later
//    private List<DisclosurePanel> collapsablePanels;
    
    private final CompetitorSelectionProvider competitorSelectionProvider;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, String raceBoardName, ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.setRaceBoardName(raceBoardName);
        this.errorReporter = errorReporter;
        // TODO Frank to use these later
//        this.stringMessages = stringMessages;

        VerticalPanel mainPanel = new VerticalPanel();
        // TODO marcus: add styles in css
        mainPanel.addStyleName("mainPanel");
        setWidget(mainPanel);
        competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, new Timer(
                /* delayBetweenAutoAdvancesInMilliseconds */500), competitorSelectionProvider, stringMessages);
        for(int i = 0; i < 4; i++) {
            if(i == 0) {
                AbsolutePanel contentPanel = new AbsolutePanel();
                DisclosurePanel panel = createDisclosePanel(contentPanel, "Panel " + i, 300);

                raceMap.loadMapsAPI(contentPanel);
                mainPanel.add(panel);
            } else {
                VerticalPanel contentPanel = new VerticalPanel();
                DisclosurePanel panel = createDisclosePanel(contentPanel, "Panel " + i, 100);
                contentPanel.add(new Label("Content of panel " + i));
                mainPanel.add(panel);
            }
        }
    }

    private DisclosurePanel createDisclosePanel(Panel contentPanel, String panelTitle, int heightInPx) {
        DisclosurePanel disclosurePanel = new DisclosurePanel (resources.openIcon(), resources.closeIcon(), panelTitle);
        disclosurePanel.setSize("100%", "100%");
        disclosurePanel.addStyleName("disclosePanel");
        disclosurePanel.setOpen(true);
        contentPanel.setSize("100%", heightInPx + "px");
        disclosurePanel.setContent(contentPanel);

        disclosurePanel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
            }
        });
        disclosurePanel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
            @Override
            public void onClose(CloseEvent<DisclosurePanel> event) {
            }
        });
        return disclosurePanel;
    }
    
    @Override
    public Widget getEntryWidget() {
        return this;
    }

    public void updateSettings(RaceBoardSettings result) {

    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected String getRaceBoardName() {
        return raceBoardName;
    }

    protected void setRaceBoardName(String raceBoardName) {
        this.raceBoardName = raceBoardName;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RaceBoardSettings> getSettingsDialogComponent() {
        return null;
    }
}
