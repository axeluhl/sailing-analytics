package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class PositionEntryDialog extends DataEntryDialog<PositionDTO> {
    private final DoubleBox lat;
    private final DoubleBox lng;
    private final StringMessages stringMessages;
    
    private static final double ERROR_VAL = Double.MIN_VALUE;

    public PositionEntryDialog(String title, final StringMessages stringMessages, DialogCallback<PositionDTO> callback) {
        super(title, title, stringMessages.save(), stringMessages.cancel(),
                new DataEntryDialog.Validator<PositionDTO>() {
                    @Override
                    public String getErrorMessage(PositionDTO valueToValidate) {
                        if (valueToValidate.getLatDeg() == ERROR_VAL) {
                            return stringMessages.pleaseEnterA(stringMessages.latitude());
                        }
                        if (valueToValidate.getLatDeg() > 90 || valueToValidate.getLatDeg() < -90) {
                            return stringMessages.pleaseEnterAValidValueFor(stringMessages.latitude(), "-90.0 - 90.0");
                        }
                        if (valueToValidate.getLngDeg() == ERROR_VAL) {
                            return stringMessages.pleaseEnterA(stringMessages.longitude());
                        }
                        if (valueToValidate.getLngDeg() > 180 || valueToValidate.getLngDeg() < -180) {
                            return stringMessages.pleaseEnterAValidValueFor(stringMessages.latitude(), "-180.0 - 180.0");
                        }
                        return null;
                    }
        }, true, callback);
        
        this.stringMessages = stringMessages;
        
        lat = createDoubleBox(10);
        lng = createDoubleBox(10);
    }

    @Override
    protected PositionDTO getResult() {
        Double latDeg = lat.getValue();
        Double lngDeg = lng.getValue();
        return new PositionDTO(latDeg == null ? ERROR_VAL : latDeg, lngDeg == null ? ERROR_VAL : lngDeg);
        
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.latitude() + " (" + stringMessages.degreesShort() + ")"));
        grid.setWidget(0, 1, lat);
        grid.setWidget(1, 0, new Label(stringMessages.longitude() + " (" + stringMessages.degreesShort() + ")"));
        grid.setWidget(1, 1, lng);
        
        return grid;
    }

}
