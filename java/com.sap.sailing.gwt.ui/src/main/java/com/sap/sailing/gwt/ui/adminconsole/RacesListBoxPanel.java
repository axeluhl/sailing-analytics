package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RacesListBoxPanel extends FormPanel implements RaceSelectionProvider, EventDisplayer {
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;
    private final List<RaceDTO> raceList;
    private final ListBox raceListBox;
    private final EventRefresher eventRefresher;
    private final StringMessages stringConstants;
    
    public RacesListBoxPanel(EventRefresher eventRefresher, StringMessages stringConstants) {
        this.eventRefresher = eventRefresher;
        this.stringConstants = stringConstants;
        this.raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        raceListBox = new ListBox();
        raceListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                fireRaceSelectionChanged(Collections.singletonList(getSelectedRace()));
            }
        });
        raceListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RaceDTO selectedRace = getSelectedRace();
                List<RaceDTO> selectedRacesCollection = Collections.emptyList();
                if (selectedRace != null) {
                    selectedRacesCollection = Collections.singletonList(selectedRace);
                }
                fireRaceSelectionChanged(selectedRacesCollection);
            }
        });
        raceList = new ArrayList<RaceDTO>();
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel labelAndRefreshButton = new HorizontalPanel();
        labelAndRefreshButton.setSpacing(20);
        vp.add(labelAndRefreshButton);
        labelAndRefreshButton.add(new Label(stringConstants.races()));
        vp.add(raceListBox);
        Button btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RacesListBoxPanel.this.eventRefresher.fillEvents();
            }
        });
        labelAndRefreshButton.add(btnRefresh);
        setWidget(vp);
    }

    private RaceDTO getSelectedRace() {
        int i = raceListBox.getSelectedIndex();
        RaceDTO result = null;
        if (i >= 0) {
            result = raceList.get(i);
        }
        return result;
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
        raceList.clear();
        raceListBox.clear();
        for (EventDTO event : result) {
            for (RegattaDTO regatta : event.regattas) {
                for (RaceDTO race : regatta.races) {
                    raceList.add(race);
                }
            }
        }
        Collections.sort(raceList, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO o1, RaceDTO o2) {
                String name1 = RacesListBoxPanel.this.toString(o1);
                String name2 = RacesListBoxPanel.this.toString(o2);
                return name1.compareTo(name2);
            }

        });
        for (RaceDTO p : raceList) {
            raceListBox.addItem(toString(p));
        }
        fireRaceSelectionChanged(getSelectedRaces());
    }

    @Override
    public List<RaceDTO> getSelectedRaces() {
        int i=0;
        List<RaceDTO> result = new ArrayList<RaceDTO>();
        for (RaceDTO triple : raceList) {
            if (raceListBox.isItemSelected(i)) {
                result.add(triple);
            }
            i++;
        }
        return result;
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.add(listener);
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.remove(listener);
    }

    private void fireRaceSelectionChanged(List<RaceDTO> selectedRaces) {
        for (RaceSelectionChangeListener listener : raceSelectionChangeListeners) {
            listener.onRaceSelectionChange(selectedRaces);
        }
    }

    private String toString(RaceDTO race) {
        return race.getEvent().name+" - "+race.name+(race.currentlyTracked ? " ("+stringConstants.tracked()+")" : "");
    }

}
