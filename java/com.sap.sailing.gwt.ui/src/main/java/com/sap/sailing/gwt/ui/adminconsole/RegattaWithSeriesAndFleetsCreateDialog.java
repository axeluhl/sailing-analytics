package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.listedit.ListEditorComposite;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RegattaWithSeriesAndFleetsCreateDialog extends RegattaWithSeriesAndFleetsDialog {
    
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    private ListEditorComposite<SeriesDTO> seriesEditor;

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
            if (boatClassNotEmpty && regattaToValidate.boatClass.getName().contains("/")) {
                errorMessage = stringMessages.boatClassNameMustNotContainSlashes();
            } else {
                for (RegattaDTO regatta : existingRegattas) {
                    if (regatta.getName().equals(regattaToValidate.getName())) {
                        unique = false;
                        break;
                    }
                }

                if (!nameNotEmpty) {
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
            List<EventDTO> existingEvents, StringMessages stringConstants, DialogCallback<RegattaDTO> callback) {
        super(new RegattaDTO(), existingEvents, stringConstants.addRegatta(), stringConstants.ok(), stringConstants,
                new RegattaParameterValidator(stringConstants, existingRegattas), callback);
        
        this.seriesEditor = new SeriesWithFleetsListEditor(Collections.<SeriesDTO>emptyList(), stringMessages, resources.removeIcon(), /*enableFleetRemoval*/true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(seriesEditor, stringMessages.series());
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
        dto.series = seriesEditor.getValue();
        return dto;
    }

}
