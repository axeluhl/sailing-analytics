package com.sap.sse.datamining.ui.client.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.settings.AdvancedDataMiningSettings.ChangeLossStrategy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class AdvancedDataMiningSettingsDialogComponent implements SettingsDialogComponent<AdvancedDataMiningSettings> {

    private AdvancedDataMiningSettings initialSettings;
    private StringMessages stringMessages;

    private CheckBox developerOptionsCheckBox;
    private Map<ChangeLossStrategy, RadioButton> changeLossStrategyButtons;

    public AdvancedDataMiningSettingsDialogComponent(AdvancedDataMiningSettings initialSettings,
            StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        Grid grid = new Grid(2, 2);
        grid.setCellPadding(5);

        developerOptionsCheckBox = dialog.createCheckbox(stringMessages.developerOptions());
        developerOptionsCheckBox.setValue(initialSettings.isDeveloperOptions());
        grid.setWidget(0, 0, developerOptionsCheckBox);
        
        Label changeLossStrategyLabel = dialog.createLabel(stringMessages.changeLossStrategy());
        changeLossStrategyLabel.setTitle(stringMessages.changeLossStrageyTooltip());
        grid.setWidget(1, 0, changeLossStrategyLabel);
        
        changeLossStrategyButtons = new HashMap<>();
        VerticalPanel changeLossStrategyButtonsPanel = new VerticalPanel();
        for (ChangeLossStrategy strategy : ChangeLossStrategy.values()) {
            String label = ChangeLossStrategyFormatter.format(strategy, stringMessages);
            RadioButton button = dialog.createRadioButton("changeLossStrategy", label);
            button.setTitle(ChangeLossStrategyFormatter.tooltipFor(strategy, stringMessages));
            changeLossStrategyButtons.put(strategy, button);
            changeLossStrategyButtonsPanel.add(button);
        }
        changeLossStrategyButtons.get(initialSettings.getChangeLossStrategy()).setValue(true);
        grid.setWidget(1, 1, changeLossStrategyButtonsPanel);

        return grid;
    }

    @Override
    public AdvancedDataMiningSettings getResult() {
        ChangeLossStrategy changeLossStrategy = null;
        for (Entry<ChangeLossStrategy, RadioButton> entry : changeLossStrategyButtons.entrySet()) {
            if (entry.getValue().getValue()) {
                changeLossStrategy = entry.getKey();
                break;
            }
        }
        Objects.requireNonNull(changeLossStrategy, "Change loss strategy mustn't be null");
        return new AdvancedDataMiningSettings(developerOptionsCheckBox.getValue(), changeLossStrategy);
    }

    @Override
    public Validator<AdvancedDataMiningSettings> getValidator() {
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }

}
