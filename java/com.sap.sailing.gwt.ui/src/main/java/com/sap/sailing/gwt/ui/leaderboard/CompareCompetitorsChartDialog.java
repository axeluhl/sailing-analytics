package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeRangeWithZoomModel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.charts.MultiChartPanel;
import com.sap.sailing.gwt.ui.shared.charts.MultiChartSettings;
import com.sap.sailing.gwt.ui.shared.components.ComponentToolbar;

/**
 * A dialog box that holds a {@link MultiChartPanel} for comparing a list of preselected competitors.
 * 
 * @author Benjamin Ebling, Axel Uhl (d043530)
 *
 */
public class CompareCompetitorsChartDialog extends DialogBoxExt {
    private final RaceSelectionProvider raceSelectionProvider;
    
    private final MultiChartPanel multiChartPanel;

    final ListBox racesListBox;
    
    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService,
            List<RegattaAndRaceIdentifier> races, final CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
            StringMessages stringConstants, ErrorReporter errorReporter) {
        super(new Label(stringConstants.close()));
        this.setPopupPosition(15, 15);
        this.setHTML(stringConstants.compareCompetitors());
        this.setWidth(Window.getClientWidth() - 250 + "px");
        this.setAnimationEnabled(true);

        raceSelectionProvider = new RaceSelectionModel();
        raceSelectionProvider.setAllRaces(races);
        
        multiChartPanel = new MultiChartPanel(sailingService, new AsyncActionsExecutor(), competitorSelectionProvider, raceSelectionProvider,
                timer, new TimeRangeWithZoomModel(), stringConstants, errorReporter, false, false);
        multiChartPanel.setSize("100%", "100%");
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setSize("100%", "100%");
        
        ComponentToolbar<MultiChartSettings> toolbar = new ComponentToolbar<MultiChartSettings>(multiChartPanel, stringConstants);
        toolbar.addSettingsButton();

        FlowPanel flowPanel = new FlowPanel();
        
        HorizontalPanel raceSelectionPanel = new HorizontalPanel();
        raceSelectionPanel.setSpacing(3);
        racesListBox = new ListBox();
        raceSelectionPanel.add(new Label("Select a race:"));
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
        raceSelectionProvider.setSelection(Collections.singletonList(selectedRace));
    }

}
