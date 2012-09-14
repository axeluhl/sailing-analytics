package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

public class WindSettingDialog extends DataEntryDialog<WindDTO> {
    private final StringMessages stringMessages;
    
    private final DoubleBox speedInKnotsBox;
    private final DoubleBox fromInDegBox;
    private final DoubleBox latDegBox;
    private final DoubleBox lngDegBox;
    private final DateBox timeBox;

    protected static class WindDataValidator implements Validator<WindDTO> {
        protected final StringMessages stringMessages;
        
        public WindDataValidator(StringMessages stringMessages){
            super();
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(WindDTO windDTO) {
            String errorMessage = null;
            
            if(windDTO.trueWindSpeedInKnots == null) {
                errorMessage = stringMessages.pleaseEnterAValue();
            } else if(windDTO.trueWindSpeedInKnots != null && (windDTO.trueWindSpeedInKnots < 0.0 || windDTO.trueWindSpeedInKnots > 100.0)) { 
                errorMessage = stringMessages.valueMustBeBetweenMinMax(stringMessages.speedInKnots(), "0", "100");
            } else if(windDTO.trueWindFromDeg == null){
                errorMessage = stringMessages.pleaseEnterAValue();
            } else if(windDTO.trueWindFromDeg != null && (windDTO.trueWindFromDeg < 0.0 || windDTO.trueWindFromDeg > 360.0)){
                errorMessage = stringMessages.valueMustBeBetweenMinMax(stringMessages.fromDeg(), "0", "360");
            } else if(windDTO.position != null) {
                if(windDTO.position.latDeg < -90.0 || windDTO.position.latDeg > 90.0){
                    errorMessage = stringMessages.valueMustBeBetweenMinMax(stringMessages.latitude(), "-90", "90");
                } else if(windDTO.position.lngDeg < -180.0 || windDTO.position.lngDeg > 180.0){
                    errorMessage = stringMessages.valueMustBeBetweenMinMax(stringMessages.longitude(), "-180", "180");
                }
            }
            
            return errorMessage;
        }
    }

    public WindSettingDialog(RaceDTO race, StringMessages stringMessages, AsyncCallback<WindDTO> callback) {
        super(stringMessages.actionAddWindData(), null, stringMessages.ok(), stringMessages.cancel(), new WindDataValidator(stringMessages), callback);
        this.stringMessages = stringMessages;        
        speedInKnotsBox = createDoubleBox(5);
        fromInDegBox = createDoubleBox(5);
        latDegBox = createDoubleBox(10);
        lngDegBox = createDoubleBox(10);
        if(race.startOfRace != null) {
            timeBox = createDateBox(race.startOfRace.getTime(), 20);
        } else {
            timeBox = createDateBox(20);
        }
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        
        Grid grid = new Grid(5, 2);
        mainPanel.add(grid);
        grid.setWidget(0, 0, new Label(stringMessages.speedInKnots() + ":"));
        grid.setWidget(0, 1, speedInKnotsBox);
        grid.setWidget(1, 0, new Label(stringMessages.fromDeg() + ":"));
        grid.setWidget(1, 1, fromInDegBox);
        grid.setWidget(2, 0, new Label(stringMessages.latitude() + " (" + stringMessages.optional() + "):"));
        grid.setWidget(2, 1, latDegBox);
        grid.setWidget(3, 0, new Label(stringMessages.longitude() + " (" + stringMessages.optional() + "):"));
        grid.setWidget(3, 1, lngDegBox);
        grid.setWidget(4, 0, new Label(stringMessages.time() + " (" + stringMessages.optional() + "):"));
        grid.setWidget(4, 1, timeBox);
        
        return mainPanel;
    }

    @Override
    protected WindDTO getResult() {
        WindDTO result = new WindDTO();

        result.trueWindSpeedInKnots = speedInKnotsBox.getValue();
        result.trueWindFromDeg = fromInDegBox.getValue();
        result.timepoint = timeBox.getValue() != null ? timeBox.getValue().getTime() : System.currentTimeMillis();
        if (latDegBox.getValue() != null && lngDegBox.getValue() != null) {
            result.position = new PositionDTO(latDegBox.getValue(), lngDegBox.getValue());
        }

        return result;
    }

    @Override
    public void show() {
        super.show();
        speedInKnotsBox.setFocus(true);
    }
}
