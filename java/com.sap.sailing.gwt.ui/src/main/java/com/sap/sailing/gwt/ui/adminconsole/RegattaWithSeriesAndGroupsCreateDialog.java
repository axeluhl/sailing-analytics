package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RegattaWithSeriesAndGroupsCreateDialog extends DataEntryDialog<RegattaDTO> {

    private StringMessages stringConstants;
    private RegattaDTO regatta;

    private TextBox nameEntryField;
    private TextBox boatClassEntryField;

    private List<TextBox> seriesNameEntryFields;

    private Grid seriesGrid;

    protected static class RegattaParameterValidator implements Validator<RegattaDTO> {

        private StringMessages stringConstants;
        private ArrayList<RegattaDTO> existingRegattas;

        public RegattaParameterValidator(StringMessages stringConstants, Collection<RegattaDTO> existingRegattas) {
            this.stringConstants = stringConstants;
            this.existingRegattas = new ArrayList<RegattaDTO>(existingRegattas);
        }

        @Override
        public String getErrorMessage(RegattaDTO regattaToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = regattaToValidate.name != null && regattaToValidate.name.length() > 0;
            boolean boatClassNotEmpty = regattaToValidate.boatClass != null && regattaToValidate.boatClass.name.length() > 0;

            boolean unique = true;
            for (RegattaDTO regatta : existingRegattas) {
                if (regatta.name.equals(regattaToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!boatClassNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!unique) {
                errorMessage = stringConstants.regattaWithThisNameAlreadyExists();
            }

            if(errorMessage == null) {
                List<SeriesDTO> seriesToValidate = regattaToValidate.series;
                int index = 0;
                boolean seriesNameNotEmpty = true;

                for (SeriesDTO series : seriesToValidate) {
                    seriesNameNotEmpty = series.name != null && series.name.length() > 0;
                    if(!seriesNameNotEmpty) {
                        break;
                    }
                    index++;
                }

                int index2 = 0;
                boolean seriesUnique = true;
                
                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (SeriesDTO series : seriesToValidate) {
                    if(!setToFindDuplicates.add(series.name)) {
                        seriesUnique = false;
                        break;
                    }
                    index2++;
                }

                if (!seriesNameNotEmpty) {
                    errorMessage = stringConstants.series() + " " + (index + 1) + ": " + stringConstants.pleaseEnterNonEmptyName();
                } else if (!seriesUnique) {
                    errorMessage = stringConstants.series() + " " + (index2 + 1) + ": " + stringConstants.seriesWithThisNameAlreadyExists();
                }
                
            }
            
            return errorMessage;
        }

    }

    public RegattaWithSeriesAndGroupsCreateDialog(Collection<RegattaDTO> existingRegattas, StringMessages stringConstants,
            AsyncCallback<RegattaDTO> callback) {
        super(stringConstants.regatta(), null, stringConstants.ok(), stringConstants.cancel(),  
                new RegattaParameterValidator(stringConstants, existingRegattas), callback);
        this.stringConstants = stringConstants;
        this.regatta = new RegattaDTO();

        nameEntryField = createTextBox(null);
        nameEntryField.setWidth("200px");
        boatClassEntryField = createTextBox(null);
        boatClassEntryField.setWidth("150px");

        seriesNameEntryFields = new ArrayList<TextBox>();
        createNewSeriesWidget(null);
        
        seriesGrid = new Grid(1, 2);
        seriesGrid.setWidget(0,  0, new Label(stringConstants.series() + " 1:"));
        seriesGrid.setWidget(0, 1, createAddGroupButton(0));
    }

    private TextBox createNewSeriesWidget(String defaultName) {
        TextBox textBox = createTextBox(defaultName); 
        textBox.setWidth("200px");
        seriesNameEntryFields.add(textBox);
        return textBox; 
    }

    @Override
    protected RegattaDTO getResult() {
        regatta.name = nameEntryField.getText();
        regatta.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);

        regatta.series = new ArrayList<SeriesDTO>();
        for(TextBox textBox: seriesNameEntryFields) {
            SeriesDTO seriesDTO = new SeriesDTO();
            seriesDTO.name = textBox.getValue();
            regatta.series.add(seriesDTO);
        }

        return regatta;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringConstants.boatClass() + ":"));
        formGrid.setWidget(1, 1, boatClassEntryField);
        
        panel.add(createHeadlineLabel(stringConstants.series()));
        panel.add(seriesGrid);
        
        Button addSeriesButton = new Button("Add series");
        addSeriesButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                createNewSeriesWidget(null);
                updateSeriesGrid(panel);
            }
        });
        panel.add(addSeriesButton);
        
        return panel;
    }

    private void updateSeriesGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(seriesGrid);
        parentPanel.remove(seriesGrid);
        
        int seriesCount = seriesNameEntryFields.size();
        seriesGrid = new Grid(seriesCount, 3);

        for(int i = 0; i < seriesCount; i++) {
            seriesGrid.setWidget(i, 0, new Label(stringConstants.series() + " " + (i+1) + ":"));
            seriesGrid.setWidget(i, 1, seriesNameEntryFields.get(i));
            seriesGrid.setWidget(i, 2, createAddGroupButton(i));
        }

        parentPanel.insert(seriesGrid, widgetIndex);
    }

    private Button createAddGroupButton(final int seriesIndex) {
        Button addGroupButton = new Button(stringConstants.add());
        addGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });
        return addGroupButton;
    }
    
    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
