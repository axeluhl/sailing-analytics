package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RegattaWithSeriesAndFleetsCreateDialog extends RegattaWithSeriesAndFleetsDialog {
    protected static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    protected static class RegattaParameterValidator extends AbstractRegattaParameterValidator {
        private ArrayList<RegattaDTO> existingRegattas;

        public RegattaParameterValidator(StringMessages stringMessages, Collection<RegattaDTO> existingRegattas) {
            super(stringMessages);
            this.existingRegattas = new ArrayList<RegattaDTO>(existingRegattas);
        }

        @Override
        public String getErrorMessage(RegattaDTO regattaToValidate) {
            String errorMessage = super.getErrorMessage(regattaToValidate);
            if (errorMessage == null) {
                boolean nameNotEmpty = regattaToValidate.getName() != null && regattaToValidate.getName().length() > 0;
                boolean boatClassNotEmpty = regattaToValidate.boatClass != null
                        && regattaToValidate.boatClass.getName().length() > 0;
                boolean unique = true;
                for (RegattaDTO regatta : existingRegattas) {
                    if (regatta.getName().equals(regattaToValidate.getName())) {
                        unique = false;
                        break;
                    }
                }
    
                Date startDate = regattaToValidate.startDate;
                Date endDate = regattaToValidate.endDate;
                String datesErrorMessage = null;
                // remark: startDate == null and endDate == null is valid
                if (startDate != null && endDate != null) {
                    if (startDate.after(endDate)) {
                        datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
                    }
                } else if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                    datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
                }

                if (datesErrorMessage != null) {
                    errorMessage = datesErrorMessage;
                } else if (!nameNotEmpty) {
                    errorMessage = stringMessages.pleaseEnterAName();
                } else if (regattaToValidate.getName().contains("/")) {
                    errorMessage = stringMessages.regattaNameMustNotContainSlashes();
                } else if (!boatClassNotEmpty) {
                    errorMessage = stringMessages.pleaseEnterABoatClass();
                } else if (!unique) {
                    errorMessage = stringMessages.regattaWithThisNameAlreadyExists();
                }
                if (errorMessage == null) {
                    List<SeriesDTO> seriesToValidate = regattaToValidate.series;
                    int index = 0;
                    boolean seriesNameNotEmpty = true;
                    for (SeriesDTO series : seriesToValidate) {
                        seriesNameNotEmpty = series.getName() != null && series.getName().length() > 0;
                        if (!seriesNameNotEmpty) {
                            break;
                        }
                        index++;
                    }
                    int index2 = 0;
                    boolean seriesUnique = true;
                    HashSet<String> setToFindDuplicates = new HashSet<String>();
                    for (SeriesDTO series : seriesToValidate) {
                        if (!setToFindDuplicates.add(series.getName())) {
                            seriesUnique = false;
                            break;
                        }
                        index2++;
                    }
                    if (!seriesNameNotEmpty) {
                        errorMessage = stringMessages.series() + " " + (index + 1) + ": "
                                + stringMessages.pleaseEnterAName();
                    } else if (!seriesUnique) {
                        errorMessage = stringMessages.series() + " " + (index2 + 1) + ": "
                                + stringMessages.seriesWithThisNameAlreadyExists();
                    }
                }
            }
            return errorMessage;
        }
    }

    public RegattaWithSeriesAndFleetsCreateDialog(Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, EventDTO correspondingEvent, final SailingServiceAsync sailingService,
            StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(new RegattaDTO(), Collections.<SeriesDTO> emptySet(), existingEvents, correspondingEvent,
                stringMessages.addRegatta(), stringMessages.ok(), sailingService, stringMessages,
                new RegattaParameterValidator(stringMessages, existingRegattas), callback);
        buoyZoneRadiusInHullLengthsDoubleBox.setValue(Regatta.DEFAULT_BUOY_ZONE_RADIUS_IN_HULL_LENGTHS);
        SeriesDTO series = new SeriesDTO();
        series.setName(Series.DEFAULT_NAME);
        series.setMedal(false);
        series.setStartsWithZeroScore(false);
        series.setSplitFleetContiguousScoring(false);
        series.setFirstColumnIsNonDiscardableCarryForward(false);
        series.setFleets(Collections.singletonList(new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null)));
        seriesEditor.setValue(Collections.singleton(series));
    }

    @Override
    protected boolean isEnableFleetRemoval() {
        return true;
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel, Grid formGrid) {
        super.setupAdditionalWidgetsOnPanel(panel, formGrid);
        insertRankingMetricTabPanel(formGrid);
        final TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(getSeriesEditor(), stringMessages.series());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return nameEntryField;
    }

    @Override
    protected RegattaDTO getResult() {
        RegattaDTO dto = super.getResult();
        List<SeriesDTO> seriesList = getSeriesEditor().getValue();
        for (SeriesDTO series : seriesList) {
            // generate 3 Default Races if default series is still present
            if (series.getName().equals(Series.DEFAULT_NAME)) {
                List<RaceColumnDTO> races = new ArrayList<RaceColumnDTO>();
                for (int i = 1; i <= 3; i++) {
                    RaceColumnDTO raceColumnDTO = new RaceColumnInSeriesDTO(series.getName(), dto.getName());
                    raceColumnDTO.setName("R"+i);
                    races.add(raceColumnDTO);
                }
                series.setRaceColumns(races);
            }
        }
        dto.series = seriesList;
        setRankingMetrics(dto);
        return dto;
    }

}
