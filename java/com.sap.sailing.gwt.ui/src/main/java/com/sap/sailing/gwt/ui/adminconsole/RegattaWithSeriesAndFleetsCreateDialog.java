package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RegattaWithSeriesAndFleetsCreateDialog extends RegattaWithSeriesAndFleetsDialog {
    
    private Grid seriesGrid;

    protected static class RegattaParameterValidator implements Validator<RegattaDTO> {

        private StringMessages stringMessages;
        private ArrayList<RegattaDTO> existingRegattas;

        public RegattaParameterValidator(StringMessages stringMessages, Collection<RegattaDTO> existingRegattas) {
            this.stringMessages = stringMessages;
            this.existingRegattas = new ArrayList<RegattaDTO>(existingRegattas);
        }

        @Override
        public String getErrorMessage(RegattaDTO regattaToValidate) {
            String errorMessage = null;
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

            if (!nameNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
            } else if (!boatClassNotEmpty) {
                errorMessage = stringMessages.pleaseEnterAName();
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

            return errorMessage;
        }

    }

    public RegattaWithSeriesAndFleetsCreateDialog(Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, StringMessages stringConstants, DialogCallback<RegattaDTO> callback) {
        super(new RegattaDTO(), existingEvents, stringConstants.addRegatta(), stringConstants.ok(), stringConstants,
                new RegattaParameterValidator(stringConstants, existingRegattas), callback);
        this.seriesGrid = new Grid(0, 0);
        this.seriesGrid.ensureDebugId("SeriesPanel");
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
        panel.add(createHeadlineLabel(stringMessages.series()));
        panel.add(seriesGrid);
        Button addSeriesButton = new Button(stringMessages.addSeries());
        addSeriesButton.ensureDebugId("AddSeriesButton");
        addSeriesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RegattaDTO result = getResult();
                SeriesWithFleetsCreateDialog dialog = new SeriesWithFleetsCreateDialog(Collections
                        .unmodifiableCollection(result.series), stringMessages, new DialogCallback<SeriesDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(SeriesDTO newSeries) {
                        series.add(newSeries);
                        updateSeriesGrid(panel);
                    }
                });
                dialog.show();
            }
        });
        panel.add(addSeriesButton);
    }

    private void updateSeriesGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(seriesGrid);
        parentPanel.remove(seriesGrid);

        int seriesCount = series.size();
        seriesGrid = new Grid(seriesCount * 2, 3);
        seriesGrid.ensureDebugId("SeriesPanel");
        seriesGrid.setCellSpacing(3);

        for (int i = 0; i < seriesCount; i++) {
            SeriesDTO seriesDTO = series.get(i);
            Label seriesLabel = new Label((i + 1) + ". " + stringMessages.series() + ":");
            seriesLabel.setWordWrap(false);
            seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            seriesGrid.setWidget(i * 2, 0, seriesLabel);
            seriesGrid.setHTML(i * 2, 1, seriesDTO.getName());
            if (seriesDTO.getFleets() != null && seriesDTO.getFleets().size() > 0) {
                seriesGrid.setHTML(i * 2 + 1, 1, seriesDTO.getFleets().size() + " fleets: "
                        + seriesDTO.getFleets().toString());
            } else {
                seriesGrid.setHTML(i * 2 + 1, 1, seriesDTO.getFleets().size() + " No fleets defined.");
            }
        }

        parentPanel.insert(seriesGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
