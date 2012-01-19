package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.server.api.DefaultLeaderboardName;

public class RaceBoardPanel extends FormPanel implements Component<RaceBoardSettings> {

    private final SailingServiceAsync sailingService;

    private final ErrorReporter errorReporter;

//    private final StringMessages stringMessages;

    private String raceBoardName;

    private final List<CollapsableComponentViewer<?>> collapsableViewers;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, String raceBoardName, ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.setRaceBoardName(raceBoardName);
        this.errorReporter = errorReporter;
        VerticalPanel mainPanel = new VerticalPanel();
        // TODO marcus: add styles in css
        mainPanel.addStyleName("mainPanel");
        setWidget(mainPanel);
        collapsableViewers = new ArrayList<CollapsableComponentViewer<?>>();
        for(int i = 0; i < 4; i++) {
            if(i == 0) {
                CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
                //final RaceMapPanel raceMapPanel = new RaceMapPanel(sailingService, competitorSelectionModel, this, this, stringMessages);
                LeaderboardPanel defaultLeaderboardPanel = new LeaderboardPanel(sailingService, competitorSelectionModel,
                        DefaultLeaderboardName.DEFAULT_LEADERBOARD_NAME, errorReporter, stringMessages);

                CollapsableComponentViewer<LeaderboardSettings> viewer = new CollapsableComponentViewer<LeaderboardSettings>(
                        defaultLeaderboardPanel, stringMessages);
                collapsableViewers.add(viewer);
            } else {
                SimpleComponentGroup<Object> componentGroup = new SimpleComponentGroup<Object>("Component Group " + i);
                componentGroup.addComponent(new SimpleComponent("My Component"));
                componentGroup.addComponent(new SimpleComponent("My Component 2"));
                componentGroup.addComponent(new SimpleComponent("My Component 3"));

                //LeaderboardPanel leaderboardPanel = LeaderboardPanel(); 
                CollapsableComponentViewer<Object> viewer = new CollapsableComponentViewer<Object>(componentGroup, stringMessages);
                collapsableViewers.add(viewer);
            }
        }
        for (CollapsableComponentViewer<?> viewer : collapsableViewers) {
            mainPanel.add(viewer.getViewerWidget());
        }
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

