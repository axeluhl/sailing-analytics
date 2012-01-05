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
import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;

public class RacesListBoxPanel extends FormPanel implements RaceSelectionProvider, EventDisplayer {
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;
    private final List<Triple<EventDAO, RegattaDAO, RaceDAO>> raceList;
    private final ListBox raceListBox;
    private final EventRefresher eventRefresher;
    private final StringConstants stringConstants;
    
    public RacesListBoxPanel(EventRefresher eventRefresher, StringConstants stringConstants) {
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
                Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace = getSelectedRace();
                List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRacesCollection = Collections.emptyList();
                if (selectedRace != null) {
                    Collections.singletonList(selectedRace);
                }
                fireRaceSelectionChanged(selectedRacesCollection);
            }
        });
        raceList = new ArrayList<Triple<EventDAO, RegattaDAO, RaceDAO>>();
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

    private Triple<EventDAO, RegattaDAO, RaceDAO> getSelectedRace() {
        int i = raceListBox.getSelectedIndex();
        Triple<EventDAO, RegattaDAO, RaceDAO> result = null;
        if (i >= 0) {
            result = raceList.get(i);
        }
        return result;
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        raceList.clear();
        raceListBox.clear();
        for (EventDAO event : result) {
            for (RegattaDAO regatta : event.regattas) {
                for (RaceDAO race : regatta.races) {
                    raceList.add(new Triple<EventDAO, RegattaDAO, RaceDAO>(event, regatta, race));
                }
            }
        }
        Collections.sort(raceList, new Comparator<Triple<EventDAO, RegattaDAO, RaceDAO>>() {
            @Override
            public int compare(Triple<EventDAO, RegattaDAO, RaceDAO> o1, Triple<EventDAO, RegattaDAO, RaceDAO> o2) {
                String name1 = RacesListBoxPanel.this.toString(o1);
                String name2 = RacesListBoxPanel.this.toString(o2);
                return name1.compareTo(name2);
            }

        });
        for (Triple<EventDAO, RegattaDAO, RaceDAO> p : raceList) {
            raceListBox.addItem(toString(p));
        }
        fireRaceSelectionChanged(getSelectedEventAndRace());
    }

    @Override
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace() {
        int i=0;
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> result = new ArrayList<Triple<EventDAO,RegattaDAO,RaceDAO>>();
        for (Triple<EventDAO, RegattaDAO, RaceDAO> triple : raceList) {
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

    private void fireRaceSelectionChanged(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        for (RaceSelectionChangeListener listener : raceSelectionChangeListeners) {
            listener.onRaceSelectionChange(selectedRaces);
        }
    }

    private String toString(Triple<EventDAO, RegattaDAO, RaceDAO> pair) {
        return pair.getA().name+" - "+pair.getC().name+(pair.getC().currentlyTracked ? " ("+stringConstants.tracked()+")" : "");
    }

}
