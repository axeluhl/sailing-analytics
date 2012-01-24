package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.adminconsole.RaceMap;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapSettings;
import com.sap.sailing.gwt.ui.adminconsole.TimePanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

/**
 * A panel showing a list of components visualizing a race from the events announced by calls to {@link #fillEvents(List)}.
 * The race selection is provided by a {@link RaceSelectionProvider} for which this is a {@link RaceSelectionChangeListener listener}.
 * {@link RaceIdentifier}-based race selection changes are converted to {@link RaceDTO} objects using the {@link #racesByIdentifier}
 * map maintained during {@link #fillEvents(List)}. The race selection provider is expected to be single selection only.
 * 
 * @author Frank Mittag, Axel Uhl (d043530)
 *
 */
public class RaceBoardPanel extends FormPanel implements Component<RaceBoardSettings>, EventDisplayer, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private String raceBoardName;
    
    /**
     * Updated upon each {@link #fillEvents(List)}
     */
    private final Map<RaceIdentifier, RaceDTO> racesByIdentifier;

    private final Timer timer;
    private final List<CollapsableComponentViewer<?>> collapsableViewers;
    private final HorizontalPanel breadcrumbPanel;
    private final TimePanel timePanel;
    private final RaceSelectionProvider raceSelectionProvider;
    
    public RaceBoardPanel(SailingServiceAsync sailingService, RaceSelectionProvider raceSelectionProvider, String leaderboardName, 
            ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.raceSelectionProvider = raceSelectionProvider;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        racesByIdentifier = new HashMap<RaceIdentifier, RaceDTO>();
        RaceIdentifier selectedRaceIdentifier = raceSelectionProvider.getSelectedRaces().iterator().next();
        this.setRaceBoardName(selectedRaceIdentifier.getRaceName());
        this.errorReporter = errorReporter;
        VerticalPanel mainPanel = new VerticalPanel();
        // TODO marcus: add styles in css
        mainPanel.addStyleName("mainPanel");
        setWidget(mainPanel);

        timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */500);
        collapsableViewers = new ArrayList<CollapsableComponentViewer<?>>();
        CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);

        breadcrumbPanel = new HorizontalPanel();
        breadcrumbPanel.setSpacing(10);
        breadcrumbPanel.addStyleName("breadcrumbPanel");
        mainPanel.add(breadcrumbPanel);
        Label eventNameLabel = new Label(selectedRaceIdentifier.getRaceName());
        eventNameLabel.addStyleName("eventNameHeadline");
        breadcrumbPanel.add(eventNameLabel);

        // create the default leaderboard and select the right race
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, selectedRaceIdentifier, competitorSelectionModel,
                leaderboardName, errorReporter, stringMessages);
        addComponentMenuEntry(leaderboardPanel);

        CollapsableComponentViewer<LeaderboardSettings> leaderboardViewer = new CollapsableComponentViewer<LeaderboardSettings>(
                leaderboardPanel, "100%", "100%", stringMessages);
        collapsableViewers.add(leaderboardViewer);

        // create the race map
        RaceMap raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel, stringMessages);
        CollapsableComponentViewer<RaceMapSettings> raceMapViewer = new CollapsableComponentViewer<RaceMapSettings>(
                raceMap, "600px", "300px", stringMessages);
        addComponentMenuEntry(raceMap);

        raceMap.loadMapsAPI((Panel) raceMapViewer.getViewerWidget().getContent());
        raceMap.onRaceSelectionChange(Collections.singletonList(selectedRaceIdentifier));
        collapsableViewers.add(raceMapViewer);
        
        // just a sample component with subcomponents
        SimpleComponentGroup<Object> componentGroup = new SimpleComponentGroup<Object>("Component Group");
        componentGroup.addComponent(new SimpleComponent("My Component"));
        componentGroup.addComponent(new SimpleComponent("My Component 2"));
        componentGroup.addComponent(new SimpleComponent("My Component 3"));
        collapsableViewers.add(new CollapsableComponentViewer<Object>(componentGroup, "100%", "100px", stringMessages));
        addComponentMenuEntry(componentGroup);

        for (CollapsableComponentViewer<?> componentViewer : collapsableViewers) {
            mainPanel.add(componentViewer.getViewerWidget());
        }
        timer.addTimeListener(leaderboardPanel);
        timer.addTimeListener(raceMap);
        timePanel = new TimePanel(stringMessages, timer);
        mainPanel.add(timePanel);
    }

    private void addComponentMenuEntry(final Component<?> c) {
//        Label menuEntry = new Label(c.getLocalizedShortName());
        Anchor menuEntry = new Anchor(c.getLocalizedShortName());
        menuEntry.addStyleName("raceBoard-menuEntry");
        
        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(timePanel);
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        
        RootPanel.get().add(timelinePanel);
        menuEntry.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                c.getEntryWidget().getElement().scrollIntoView();
            }
        });
        breadcrumbPanel.add(menuEntry);
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

    @Override
    public void fillEvents(List<EventDTO> events) {
        racesByIdentifier.clear();
        for (EventDTO event : events) {
            for (RegattaDTO regatta : event.regattas) {
                for (RaceDTO race : regatta.races) {
                    if (race != null && race.getRaceIdentifier() != null) {
                        racesByIdentifier.put(race.getRaceIdentifier(), race);
                    }
                }
            }
        }
        // trigger selection change because now racesByIdentifier may have the information required, e.g., to update the time slider
        onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            RaceDTO selectedRace = racesByIdentifier.get(selectedRaces.iterator().next());
            if (selectedRace.startOfRace != null) {
                timer.setTime(selectedRace.startOfRace.getTime());
            }
            if (selectedRace.startOfTracking != null) {
                timePanel.setMin(selectedRace.startOfTracking);
            }
            if (selectedRace.timePointOfNewestEvent != null) {
                timePanel.setMax(selectedRace.timePointOfNewestEvent);
            }
        }
    }
}

