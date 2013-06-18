package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Allows administrators to manage the structure of a regatta. Each regatta consists of several substructures like
 * races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 * 
 */
public class RegattaStructureManagementPanel extends SimplePanel implements RegattaDisplayer, RegattaSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final RegattaRefresher regattaRefresher;
    private RegattaSelectionProvider regattaSelectionProvider;

    private RegattaListComposite regattaListComposite;
    private RegattaDetailsComposite regattaDetailsComposite;
    
    public RegattaStructureManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;

        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        Button addRegattaBtn = new Button(stringMessages.addRegatta());
        mainPanel.add(addRegattaBtn);
        addRegattaBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateRegattaDialog();
            }
        });

        Grid grid = new Grid(1 ,2);
        mainPanel.add(grid);
        
        regattaSelectionProvider = new RegattaSelectionModel(false);
        regattaSelectionProvider.addRegattaSelectionChangeListener(this);
        
        regattaListComposite = new RegattaListComposite(sailingService, regattaSelectionProvider, regattaRefresher, errorReporter, stringMessages);
        grid.setWidget(0, 0, regattaListComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        regattaDetailsComposite = new RegattaDetailsComposite(sailingService, regattaRefresher, errorReporter, stringMessages);
        regattaDetailsComposite.setVisible(false);
        grid.setWidget(0, 1, regattaDetailsComposite);
    }

    private void openCreateRegattaDialog() {
        final Collection<RegattaDTO> existingRegattas = Collections.unmodifiableCollection(regattaListComposite.getAllRegattas());

        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                openCreateRegattaDialog(existingRegattas, Collections.<EventDTO>emptyList());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                openCreateRegattaDialog(existingRegattas, Collections.unmodifiableList(result));
            }
        });
    }

    private void openCreateRegattaDialog(Collection<RegattaDTO> existingRegattas, List<EventDTO> existingEvents) {
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, existingEvents, stringMessages,
                new DialogCallback<RegattaDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(RegattaDTO newRegatta) {
                createNewRegatta(newRegatta);
            }
        });
        dialog.show();
    }
    
    private void createNewRegatta(final RegattaDTO newRegatta) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesStructure = new LinkedHashMap<String, SeriesCreationParametersDTO>();
        for (SeriesDTO seriesDTO : newRegatta.series) {
            SeriesCreationParametersDTO seriesPair =
                    new SeriesCreationParametersDTO(
                    seriesDTO.getFleets(), seriesDTO.isMedal(), seriesDTO.isStartsWithZeroScore(), seriesDTO.isFirstColumnIsNonDiscardableCarryForward(), seriesDTO.getDiscardThresholds());
            seriesStructure.put(seriesDTO.getName(), seriesPair);
        }
        sailingService.createRegatta(newRegatta.getName(), newRegatta.boatClass==null?null:newRegatta.boatClass.getName(),
                new RegattaCreationParametersDTO(seriesStructure), true,
                newRegatta.scoringScheme, newRegatta.defaultCourseAreaIdAsString, new AsyncCallback<RegattaDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new regatta " + newRegatta.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(RegattaDTO regatta) {
                regattaRefresher.fillRegattas();
            }
        });
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas(regattas);
    }

    @Override
    public void onRegattaSelectionChange(List<RegattaIdentifier> selectedRegattas) {
        final RegattaIdentifier selectedRegatta;
        if (selectedRegattas.iterator().hasNext()) {
            selectedRegatta = selectedRegattas.iterator().next();
        if (selectedRegatta != null && regattaListComposite.getAllRegattas() != null) {
            for(RegattaDTO regattaDTO: regattaListComposite.getAllRegattas()) {
                if(regattaDTO.getRegattaIdentifier().equals(selectedRegatta)) {
                    regattaDetailsComposite.setRegatta(regattaDTO);
                    regattaDetailsComposite.setVisible(true);
                    break;
                }
            }
        }
        } else {
            regattaDetailsComposite.setRegatta(null);
            regattaDetailsComposite.setVisible(false);
        }
    }
}
