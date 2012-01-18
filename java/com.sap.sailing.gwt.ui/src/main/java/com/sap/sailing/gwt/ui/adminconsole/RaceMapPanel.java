package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.SmallWindHistoryPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final RacesListBoxPanel raceListBox;
    private final TimePanel timePanel;
    private final Timer timer;
    private final RaceMap raceMap;
    private final QuickRanksListBoxComposite quickRanksListBox;
    
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RaceMapPanel(SailingServiceAsync sailingService, CompetitorSelectionProvider competitorSelectionProvider,
            ErrorReporter errorReporter, final EventRefresher eventRefresher, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */500);
        this.grid = new Grid(3, 3);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        grid.getCellFormatter().setHeight(2, 1, "100%");
        AbsolutePanel mapPanel = new AbsolutePanel();
        mapPanel.setSize("100%", "100%");
        grid.setWidget(2, 1, mapPanel);
        raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionProvider, stringMessages);
        raceMap.loadMapsAPI(mapPanel);
        raceListBox = new RacesListBoxPanel(eventRefresher, stringMessages);
        raceListBox.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, raceListBox);
        PositionDAO pos = new PositionDAO();
        if (!raceMap.boatMarkers.isEmpty()) {
            LatLng latLng = raceMap.boatMarkers.values().iterator().next().getLatLng();
            pos.latDeg = latLng.getLatitude();
            pos.lngDeg = latLng.getLongitude();
        }
        SmallWindHistoryPanel windHistory = new SmallWindHistoryPanel(sailingService, pos,
        /* number of wind displays */5,
        /* time interval between displays in milliseconds */5000, stringMessages, errorReporter);
        raceListBox.addRaceSelectionChangeListener(windHistory);
        grid.setWidget(1, 0, windHistory);

        ImageResource settingsImage = resources.settingsIcon();
        Anchor showConfigAnchor = new Anchor(AbstractImagePrototype.create(settingsImage).getSafeHtml());
        showConfigAnchor.setTitle(stringMessages.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RaceMapSettings>(raceMap, stringMessages).show();
            }
        });
        grid.setWidget(1, 2, showConfigAnchor);
        HorizontalPanel horizontalRanksVerticalAndCheckboxesManeuversPanel = new HorizontalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.setSpacing(15);
        VerticalPanel ranksAndCheckboxAndTailLengthPanel = new VerticalPanel();
        quickRanksListBox = new QuickRanksListBoxComposite(competitorSelectionProvider);
        quickRanksListBox.getListBox().setVisibleItemCount(20);
        ranksAndCheckboxAndTailLengthPanel.add(quickRanksListBox);
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(ranksAndCheckboxAndTailLengthPanel);
        VerticalPanel verticalPanelRadioAndCheckboxes = new VerticalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(verticalPanelRadioAndCheckboxes);
        grid.setWidget(2, 0, horizontalRanksVerticalAndCheckboxesManeuversPanel);
        timePanel = new TimePanel(stringMessages, timer);
        timer.addTimeListener(raceMap);
        timer.addTimeListener(this);
        timer.addTimeListener(windHistory);
        grid.setWidget(1, 1, timePanel);
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        raceListBox.fillEvents(result);
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (!selectedRaces.isEmpty() && selectedRaces.get(selectedRaces.size() - 1) != null) {
            RaceDAO raceDAO = selectedRaces.get(selectedRaces.size() - 1).getC();
            competitorSelectionProvider.setCompetitors(raceDAO.competitors);
            if (raceDAO.startOfRace != null) {
                timePanel.timeChanged(raceDAO.startOfRace);
                timer.setTime(raceDAO.startOfRace.getTime());
            }
            updateSlider(raceDAO);
        }
        raceMap.onRaceSelectionChange(selectedRaces);
    }

    private void updateSlider(RaceDAO selectedRace) {
        if (selectedRace.startOfTracking != null) {
            timePanel.setMin(selectedRace.startOfTracking);
        }
        if (selectedRace.timePointOfNewestEvent != null) {
            timePanel.setMax(selectedRace.timePointOfNewestEvent);
        }
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = raceListBox.getSelectedEventAndRace();
            if (!selection.isEmpty()) {
                EventDAO event = selection.get(selection.size() - 1).getA();
                RaceDAO race = selection.get(selection.size() - 1).getC();
                if (event != null && race != null) {
                    sailingService.getQuickRanks(new EventNameAndRaceName(event.name, race.name), date,
                            new AsyncCallback<List<QuickRankDAO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining quick rankings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<QuickRankDAO> result) {
                                    quickRanksListBox.fillQuickRanks(result);
                                }
                            });
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.RequiresResize#onResize()
     */
    @Override
    public void onResize() {
        // handle what is required by @link{ProvidesResize}
        Widget child = getWidget();
        if (child instanceof RequiresResize) {
            ((RequiresResize) child).onResize();
        }
        // and ensure the map (indirect child) is also informed about resize
        if (raceMap.map != null) {
            raceMap.map.onResize();
        }
    }
}
