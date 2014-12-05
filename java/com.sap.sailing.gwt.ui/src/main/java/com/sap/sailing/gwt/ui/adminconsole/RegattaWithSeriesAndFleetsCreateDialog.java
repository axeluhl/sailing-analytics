package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

public class RegattaWithSeriesAndFleetsCreateDialog extends RegattaWithSeriesAndFleetsDialog {

    protected static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

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

            Date startDate = regattaToValidate.startDate;
            Date endDate = regattaToValidate.endDate;
            String datesErrorMessage = null;
            // remark: startDate == null and endDate == null is valid
            if(startDate != null && endDate != null) {
                if(startDate.after(endDate)) {
                    datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate(); 
                }
            } else if((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
                datesErrorMessage = stringMessages.pleaseEnterStartAndEndDate();
            }
            
            if(datesErrorMessage != null) {
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
            return errorMessage;
        }

    }

    public RegattaWithSeriesAndFleetsCreateDialog(Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(new RegattaDTO(), existingEvents, stringMessages.addRegatta(), stringMessages.ok(), stringMessages,
                new RegattaParameterValidator(stringMessages, existingRegattas), callback);
    }

    @Override
    protected ListEditorComposite<SeriesDTO> createSeriesEditor() {
        return new SeriesWithFleetsListEditor(Collections.<SeriesDTO> emptyList(), stringMessages, resources.removeIcon(), /* enableFleetRemoval */true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
        super.setupAdditionalWidgetsOnPanel(panel);
        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(getSeriesEditor(), stringMessages.series());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

    @Override
    protected RegattaDTO getResult() {
        RegattaDTO dto = super.getResult();
        dto.series = getSeriesEditor().getValue();
        return dto;
    }

}
