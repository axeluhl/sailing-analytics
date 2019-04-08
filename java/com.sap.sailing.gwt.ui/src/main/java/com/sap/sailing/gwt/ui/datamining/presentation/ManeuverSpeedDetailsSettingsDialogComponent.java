package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettings;
import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsSettingsImpl;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsSettingsDialogComponent
        implements SettingsDialogComponent<ManeuverSpeedDetailsSettings> {

    private ManeuverSpeedDetailsSettings settings;
    private StringMessages stringMessages;
    private RadioButton maneuverDirectionNoNormalizationRadioButton;
    private RadioButton maneuverDirectionStarboardNormalizationRadioButton;
    private RadioButton maneuverDirectionPortNormalizationRadioButton;
    private CheckBox maneuverDirectionEqualWeightingEnabledCheckBox;

    public ManeuverSpeedDetailsSettingsDialogComponent(ManeuverSpeedDetailsSettings settings) {
        this.settings = settings;
        this.stringMessages = StringMessages.INSTANCE;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        Grid grid = new Grid(2, 2);
        grid.setCellPadding(5);
        vp.add(grid);
        setupGrid(grid, dialog);
        return vp;
    }

    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
        Label directionNormalizationLabel = dialog.createLabel(stringMessages.maneuverDirectionNormalization());
        grid.setWidget(0, 0, directionNormalizationLabel);
        maneuverDirectionNoNormalizationRadioButton = dialog.createRadioButton("maneuverDirectionNormalization",
                stringMessages.disabled());
        maneuverDirectionStarboardNormalizationRadioButton = dialog.createRadioButton("maneuverDirectionNormalization",
                stringMessages.starboardSide());
        maneuverDirectionPortNormalizationRadioButton = dialog.createRadioButton("maneuverDirectionNormalization",
                stringMessages.portSide());
        if (settings.getNormalizedManeuverDirection() == NauticalSide.STARBOARD) {
            maneuverDirectionStarboardNormalizationRadioButton.setValue(true);
        } else if (settings.getNormalizedManeuverDirection() == NauticalSide.PORT) {
            maneuverDirectionPortNormalizationRadioButton.setValue(true);
        } else {
            maneuverDirectionNoNormalizationRadioButton.setValue(true);
        }
        VerticalPanel vp = new VerticalPanel();
        vp.add(maneuverDirectionNoNormalizationRadioButton);
        vp.add(maneuverDirectionStarboardNormalizationRadioButton);
        vp.add(maneuverDirectionPortNormalizationRadioButton);
        grid.setWidget(0, 1, vp);

        Label maneuverDirectionEqualWeightingEnabledLabel = dialog
                .createLabel(stringMessages.maneuverDirectionEqualWeightingEnabled());
        grid.setWidget(1, 0, maneuverDirectionEqualWeightingEnabledLabel);
        maneuverDirectionEqualWeightingEnabledCheckBox = dialog.createCheckbox("");
        maneuverDirectionEqualWeightingEnabledCheckBox.setValue(settings.isManeuverDirectionEqualWeightingEnabled());
        grid.setWidget(1, 1, maneuverDirectionEqualWeightingEnabledCheckBox);
    }

    @Override
    public ManeuverSpeedDetailsSettings getResult() {
        NauticalSide nauticalSide;
        if (maneuverDirectionPortNormalizationRadioButton.getValue()) {
            nauticalSide = NauticalSide.PORT;
        } else if (maneuverDirectionStarboardNormalizationRadioButton.getValue()) {
            nauticalSide = NauticalSide.STARBOARD;
        } else {
            nauticalSide = null;
        }
        return new ManeuverSpeedDetailsSettingsImpl(nauticalSide,
                maneuverDirectionEqualWeightingEnabledCheckBox.getValue());
    }

    @Override
    public FocusWidget getFocusWidget() {
        return maneuverDirectionNoNormalizationRadioButton;
    }

    @Override
    public Validator<ManeuverSpeedDetailsSettings> getValidator() {
        return null;
    }

}
