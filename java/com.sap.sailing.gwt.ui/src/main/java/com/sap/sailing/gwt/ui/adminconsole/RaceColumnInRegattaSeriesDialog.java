package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RaceColumnInRegattaSeriesDialog extends DataEntryDialog<Pair<RaceColumnDTO, SeriesDTO>> {
    private final TextBox raceNameBox;
    private final RaceColumnDTO raceInRegattaSeries;
    private final RegattaDTO regatta;
    private final ListBox seriesListBox;
    private final StringMessages stringConstants;

    private static class RaceDialogValidator implements Validator<Pair<RaceColumnDTO, SeriesDTO>> {
        private StringMessages stringConstants;

        public RaceDialogValidator(RegattaDTO regatta, StringMessages stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public String getErrorMessage(Pair<RaceColumnDTO, SeriesDTO> valueToValidate) {
            // hack
            if(valueToValidate.getB() == null)
                return  null;
            
            String errorMessage;
            String newRaceName = valueToValidate.getA().getRaceColumnName();
            boolean isNameNotEmpty = newRaceName != null & newRaceName != "";

            SeriesDTO series = valueToValidate.getB();
            boolean unique = true;
            for (RaceColumnDTO raceColumnInSeries: series.getRaceColumns()) {
                if (raceColumnInSeries.equals(newRaceName)) {
                    unique = false;
                }
            }

            if (!isNameNotEmpty) {
                errorMessage = stringConstants.raceNameEmpty();
            } else if (!unique) {
                errorMessage = stringConstants.raceWithThisNameAlreadyExists();
            } else {
                return errorMessage = null;
            }
            return errorMessage;
        }

    }

    public RaceColumnInRegattaSeriesDialog(RegattaDTO regatta, RaceColumnDTO raceInRegattaSeries, StringMessages stringConstants,
            AsyncCallback<Pair<RaceColumnDTO, SeriesDTO>> callback) {
        super(stringConstants.name(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(regatta, stringConstants), callback);
        this.regatta = regatta;
        this.raceInRegattaSeries = raceInRegattaSeries;
        this.stringConstants = stringConstants;
        raceNameBox = createTextBox(raceInRegattaSeries.getRaceColumnName());
        seriesListBox = createListBox(false);
    }

    @Override
    protected Pair<RaceColumnDTO, SeriesDTO> getResult() {
        raceInRegattaSeries.setRaceColumnName(raceNameBox.getValue());
        return new Pair<RaceColumnDTO, SeriesDTO>(raceInRegattaSeries, getSelectedSeries());
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(raceNameBox);
        
        HorizontalPanel seriesPanel = new HorizontalPanel();
        seriesPanel.setSpacing(3);
        seriesPanel.add(new Label(stringConstants.series() + ":"));
        seriesListBox.addItem("Please select a series");
        for (SeriesDTO series : regatta.series) {
            seriesListBox.addItem(series.name);
        }
        seriesPanel.add(seriesListBox);
        panel.add(seriesPanel);

        return panel;
    }

    private SeriesDTO getSelectedSeries() {
        SeriesDTO result = null;
        int selIndex = seriesListBox.getSelectedIndex();
        if(selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = seriesListBox.getItemText(selIndex);
            for(SeriesDTO seriesDTO: regatta.series) {
                if(seriesDTO.name.equals(itemText)) {
                    result = seriesDTO;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void show() {
        super.show();
        raceNameBox.setFocus(true);
    }

}
