package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class RaceTreeView extends FormPanel implements EventDisplayer, RaceSelectionProvider {
    private ListDataProvider<EventDAO> eventsList;
    
    private TrackedEventsTreeModel trackedEventsModel;
    
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;

    private final boolean multiSelection;

    public RaceTreeView(StringConstants stringConstants, boolean multiSelection) {
        this.multiSelection = multiSelection;
        this.raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        setWidget(new Label(stringConstants.noRacesYet()));
    }

    @Override
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace() {
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> result = new ArrayList<Triple<EventDAO,RegattaDAO,RaceDAO>>();
        for (EventDAO event : eventsList.getList()) {
            // scan event's races:
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    if ((multiSelection && trackedEventsModel.getSelectionModel().isSelected(event))
                            || (multiSelection && trackedEventsModel.getSelectionModel().isSelected(regatta))
                            || trackedEventsModel.getSelectionModel().isSelected(race)) {
                        result.add(new Triple<EventDAO, RegattaDAO, RaceDAO>(event, regatta, race));
                    }
                }
            }
        }
        return result;
    }
    
    public void selectRaceByName(String eventName, String raceName) {
        if (eventsList != null) {
            for (EventDAO event : eventsList.getList()) {
                for (RegattaDAO regatta : event.regattas) {
                    for (RaceDAO race : regatta.races) {
                        if (event.name.equals(eventName) && race.name.equals(raceName)) {
                            trackedEventsModel.getSelectionModel().setSelected(race, true);
                        }
                    }
                }
            }
        }
    }
    
    public void clearSelection() {
        if (eventsList != null) {
            for (EventDAO event : eventsList.getList()) {
                trackedEventsModel.getSelectionModel().setSelected(event, false);
                for (RegattaDAO regatta : event.regattas) {
                    trackedEventsModel.getSelectionModel().setSelected(regatta, false);
                    for (RaceDAO race : regatta.races) {
                        trackedEventsModel.getSelectionModel().setSelected(race, false);
                    }
                }
            }
        }
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.add(listener);
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.remove(listener);
    }

    private void fireRaceSelectionChanged(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        for (RaceSelectionChangeListener listener : raceSelectionChangeListeners) {
            listener.onRaceSelectionChange(selectedRaces);
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        if (!result.isEmpty()) {
            eventsList = new ListDataProvider<EventDAO>(result);
            trackedEventsModel = new TrackedEventsTreeModel(eventsList, multiSelection);
            // When the following line is uncommented, the race table contents don't show anymore
            // if there are no events yet...???!!!
            CellTree eventsCellTree = new CellTree(trackedEventsModel, /* root */null);
            eventsCellTree.setAnimationEnabled(true);
            trackedEventsModel.getSelectionModel().addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    fireRaceSelectionChanged(getSelectedEventAndRace());
                }
            });
            setWidget(eventsCellTree);
        }
    }

}
