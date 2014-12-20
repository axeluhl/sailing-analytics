package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RacesListBoxPanel extends FormPanel implements RegattasDisplayer, RaceSelectionChangeListener {
    private final List<RaceDTO> raceList;
    private final ListBox raceListBox;
    private final RegattaRefresher regattaRefresher;
    private final StringMessages stringConstants;
    private final RaceSelectionProvider raceSelectionProvider;
    
    public RacesListBoxPanel(RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider, StringMessages stringConstants) {
        this.regattaRefresher = regattaRefresher;
        this.stringConstants = stringConstants;
        this.raceSelectionProvider = raceSelectionProvider;
        raceListBox = new ListBox();
        raceListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                RacesListBoxPanel.this.raceSelectionProvider.setSelection(getSelectedRaceIdentifiers(), RacesListBoxPanel.this);
            }
        });
        raceListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RacesListBoxPanel.this.raceSelectionProvider.setSelection(getSelectedRaceIdentifiers(), RacesListBoxPanel.this);
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
                RacesListBoxPanel.this.regattaRefresher.fillRegattas();
            }
        });
        labelAndRefreshButton.add(btnRefresh);
        setWidget(vp);
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> result) {
        raceList.clear();
        raceListBox.clear();
        for (RegattaDTO regatta : result) {
            for (RaceDTO race : regatta.races) {
                raceList.add(race);
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
        raceSelectionProvider.setSelection(getSelectedRaceIdentifiers(), this);
    }

    private List<RegattaAndRaceIdentifier> getSelectedRaceIdentifiers() {
        List<RegattaAndRaceIdentifier> result = new ArrayList<RegattaAndRaceIdentifier>();
        for (RaceDTO selectedRace : getSelectedRaces()) {
            result.add(selectedRace.getRaceIdentifier());
        }
        return result;
    }
    
    private List<RaceDTO> getSelectedRaces() {
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

    private String toString(RaceDTO race) {
        return race.getRegattaName()+" - "+race.getName()+(race.isTracked ? " ("+stringConstants.tracked()+")" : "");
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            RaceDTO newSelection = getRace(selectedRaces.iterator().next());
            int index = raceList.indexOf(newSelection);
            if (index >= 0) {
                raceListBox.setSelectedIndex(index);
            } else {
                deselectAll();
            }
        } else {
            deselectAll();
        }
    }

    private void deselectAll() {
        for (int i=0; i<raceListBox.getItemCount(); i++) {
            raceListBox.setItemSelected(i, false);
        }
    }
    
    public List<RaceDTO> getAllRaces() {
        return Collections.unmodifiableList(raceList);
    }
    
    public RaceDTO getRace(RaceIdentifier raceIdentifier) {
        for (RaceDTO race : getAllRaces()) {
            if (raceIdentifier.equals(race.getRaceIdentifier())) {
                return race;
            }
        }
        return null;
    }

}
