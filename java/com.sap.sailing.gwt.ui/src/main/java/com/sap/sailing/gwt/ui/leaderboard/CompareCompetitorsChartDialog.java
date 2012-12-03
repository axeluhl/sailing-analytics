package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesCalculationUtil;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeRangeChangeListener;
import com.sap.sailing.gwt.ui.client.TimeRangeWithZoomModel;
import com.sap.sailing.gwt.ui.client.TimeRangeWithZoomProvider;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.charts.MultiChartPanel;
import com.sap.sailing.gwt.ui.shared.charts.MultiChartSettings;
import com.sap.sailing.gwt.ui.shared.components.ComponentToolbar;

/**
 * A dialog box that holds a {@link MultiChartPanel} for comparing a list of preselected competitors.
 * 
 * @author Benjamin Ebling, Axel Uhl (d043530)
 *
 */
public class CompareCompetitorsChartDialog extends DialogBoxExt implements RaceTimesInfoProviderListener {
    private final RaceSelectionProvider raceSelectionProvider;
    private final SailingServiceAsync sailingService;
    private final TimeRangeWithZoomProvider timeRangeWithZoomProvider;
    private final Timer timer;
    private final ErrorReporter errorReporter;
    
    private final MultiChartPanel multiChartPanel;

    private final ListBox racesListBox;
    
    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService,
            List<RegattaAndRaceIdentifier> races, final RaceTimesInfoProvider raceTimesInfoProvider, final CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, StringMessages stringMessages, ErrorReporter errorReporter) {
        super(new Label(stringMessages.close()));
        this.sailingService = sailingService;
        this.timer = timer;
        this.errorReporter = errorReporter;
        this.timeRangeWithZoomProvider = new TimeRangeWithZoomModel(); 
        this.setPopupPosition(15, 15);
        this.setHTML(stringMessages.competitorCharts());
        this.setWidth(Window.getClientWidth() - 250 + "px");
        this.setAnimationEnabled(true);

        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
        raceSelectionProvider = new RaceSelectionModel();
        raceSelectionProvider.setAllRaces(races);
        
        multiChartPanel = new MultiChartPanel(sailingService, new AsyncActionsExecutor(), competitorSelectionProvider, raceSelectionProvider,
                timer, timeRangeWithZoomProvider, stringMessages, errorReporter, false, false);
        multiChartPanel.setSize("100%", "100%");
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setSize("100%", "100%");
        
        ComponentToolbar<MultiChartSettings> toolbar = new ComponentToolbar<MultiChartSettings>(multiChartPanel, stringMessages);
        toolbar.addSettingsButton();

        FlowPanel flowPanel = new FlowPanel();
        
        HorizontalPanel raceSelectionPanel = new HorizontalPanel();
        raceSelectionPanel.setSpacing(3);
        racesListBox = new ListBox();
        raceSelectionPanel.add(new Label(stringMessages.pleaseSelectARace() + ":"));
        raceSelectionPanel.add(racesListBox);
        for (RegattaAndRaceIdentifier race : raceSelectionProvider.getAllRaces()) {
            racesListBox.addItem(race.toString());
        }
        racesListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selIndex = racesListBox.getSelectedIndex();
                String selItem = racesListBox.getItemText(selIndex);
                for (RegattaAndRaceIdentifier race : raceSelectionProvider.getAllRaces()) {
                    if(selItem.equals(race.toString())) {
                        selectRace(race);
                        break;
                    }
                }
            }
        });
        
        RegattaAndRaceIdentifier firstRace = raceSelectionProvider.getAllRaces().iterator().next();
        racesListBox.setSelectedIndex(0);
        selectRace(firstRace);

        flowPanel.add(raceSelectionPanel);
        raceSelectionPanel.getElement().getStyle().setFloat(Float.LEFT);
        flowPanel.add(toolbar);
        toolbar.getElement().getStyle().setFloat(Float.RIGHT);
        toolbar.setSpacing(0);
        contentPanel.add(flowPanel);
        contentPanel.add(multiChartPanel.getEntryWidget());
        
        this.setWidget(contentPanel);
    }
    
    private void selectRace(final RegattaAndRaceIdentifier selectedRace) {
        sailingService.getRaceTimesInfo(selectedRace, new AsyncCallback<RaceTimesInfoDTO>() {
            @Override
            public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                updateMinMax(raceTimesInfo);
                raceSelectionProvider.setSelection(Collections.singletonList(selectedRace));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining race times: " + caught.getMessage(), true /*silentMode */);
            }
        });
        
    }

    private void updateMinMax(RaceTimesInfoDTO newRaceTimesInfo) {
        Pair<Date, Date> raceMinMax = RaceTimesCalculationUtil.caluclateRaceMinMax(timer, newRaceTimesInfo);
        
        Date min = raceMinMax.getA();
        Date max = raceMinMax.getB();
        
        // never reduce max if it was already set
        if (min != null && max != null && (timeRangeWithZoomProvider.getToTime() == null || timeRangeWithZoomProvider.getToTime().before(max))) {
            timeRangeWithZoomProvider.setTimeRange(min, max, new TimeRangeChangeListener[0]);
        }
    }

    private RegattaAndRaceIdentifier getSelectedRace() {
        return raceSelectionProvider.getSelectedRaces().size() > 0 ? raceSelectionProvider.getSelectedRaces().get(0) : null;
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {
        RegattaAndRaceIdentifier selectedRace = getSelectedRace();
        if(selectedRace != null) {
            updateMinMax(raceTimesInfos.get(selectedRace));
        }
    }

}
