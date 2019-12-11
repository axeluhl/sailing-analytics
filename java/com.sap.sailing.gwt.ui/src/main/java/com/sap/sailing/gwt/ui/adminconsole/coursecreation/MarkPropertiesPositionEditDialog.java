package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.MarkPropertiesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DoubleBox;

public class MarkPropertiesPositionEditDialog extends DataEntryDialog<MarkPropertiesDTO> {

    private final MarkPropertiesDTO markPropertiesToEdit;
    private final DoubleBox latDoubleBox;
    private final DoubleBox lngDoubleBox;
    private final StringMessages stringMessages;

    private static final double ERROR_VAL = Double.MIN_VALUE;

    public MarkPropertiesPositionEditDialog(final StringMessages stringMessages, MarkPropertiesDTO markPropertiesToEdit,
            DialogCallback<MarkPropertiesDTO> callback) {
        super(stringMessages.edit() + " " + stringMessages.markProperties() + ": " + stringMessages.setPosition(), null,
                stringMessages.ok(), stringMessages.cancel(), new Validator<MarkPropertiesDTO>() {
                    @Override
                    public String getErrorMessage(MarkPropertiesDTO valueToValidate) {
                        if (valueToValidate.getPosition().getLatRad() == ERROR_VAL) {
                            return stringMessages.pleaseEnterA(stringMessages.latitude());
                        }
                        if (valueToValidate.getPosition().getLngRad() == ERROR_VAL) {
                            return stringMessages.pleaseEnterA(stringMessages.longitude());
                        }
                        return null;
                    }
                }, /* animationEnabled */true, callback);
        this.ensureDebugId("MarkPropertiesPositionEditDialog");
        this.stringMessages = stringMessages;
        this.markPropertiesToEdit = markPropertiesToEdit;
        this.latDoubleBox = createDoubleBox(10);
        this.lngDoubleBox = createDoubleBox(10);
    }

    @Override
    protected MarkPropertiesDTO getResult() {
        return new MarkPropertiesDTO(markPropertiesToEdit.getUuid(), markPropertiesToEdit.getName(),
                markPropertiesToEdit.getTags(), /* deviceIdentifier */ null,
                new RadianPosition(latDoubleBox.getValue() != null ? latDoubleBox.getValue() : ERROR_VAL,
                        lngDoubleBox.getValue() != null ? lngDoubleBox.getValue() : ERROR_VAL),
                markPropertiesToEdit.getCommonMarkProperties().getShortName(),
                markPropertiesToEdit.getCommonMarkProperties().getColor(),
                markPropertiesToEdit.getCommonMarkProperties().getShape(),
                markPropertiesToEdit.getCommonMarkProperties().getPattern(),
                markPropertiesToEdit.getCommonMarkProperties().getType());
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.latitude() + " (" + stringMessages.angleInRadian() + ")"));
        grid.setWidget(0, 1, latDoubleBox);
        grid.setWidget(1, 0, new Label(stringMessages.longitude() + " (" + stringMessages.angleInRadian() + ")"));
        grid.setWidget(1, 1, lngDoubleBox);
        return grid;
    }
}
