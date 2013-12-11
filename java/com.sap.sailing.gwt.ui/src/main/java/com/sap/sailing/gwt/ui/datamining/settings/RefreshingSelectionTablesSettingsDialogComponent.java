package com.sap.sailing.gwt.ui.datamining.settings;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.IntegerBox;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

public class RefreshingSelectionTablesSettingsDialogComponent implements SettingsDialogComponent<RefreshingSelectionTablesSettings> {
    
    private RefreshingSelectionTablesSettings initialSettings;
    private StringMessages stringMessages;
    
    private CheckBox refreshAutomatically;
    private IntegerBox refreshIntervalInSeconds;
    private CheckBox rerunQueryAfterRefresh;
    

    public RefreshingSelectionTablesSettingsDialogComponent(RefreshingSelectionTablesSettings initialSettings, StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel additionalWidget = new FlowPanel();
        
        refreshAutomatically = dialog.createCheckbox(stringMessages.refresh());
        refreshAutomatically.setValue(initialSettings.isRefreshAutomatically());
        additionalWidget.add(refreshAutomatically);
        
        FlowPanel subSettingsPanel = new FlowPanel();
        subSettingsPanel.getElement().getStyle().setMarginLeft(10, Unit.PX);
        additionalWidget.add(subSettingsPanel);
        
        HorizontalPanel refreshIntervalPanel = new HorizontalPanel();
        refreshIntervalPanel.setSpacing(5);
        subSettingsPanel.add(refreshIntervalPanel);
        refreshIntervalPanel.add(dialog.createLabel(stringMessages.refreshInterval()));
        refreshIntervalInSeconds = dialog.createIntegerBox(initialSettings.getRefreshIntervalInMilliseconds() / 1000, 4);
        refreshIntervalPanel.add(refreshIntervalInSeconds);
        refreshIntervalPanel.add(dialog.createLabel(stringMessages.secondsUnit()));
        
        rerunQueryAfterRefresh = dialog.createCheckbox(stringMessages.rerunQueryAfterRefresh());
        rerunQueryAfterRefresh.setValue(initialSettings.isRerunQueryAfterRefresh());
        subSettingsPanel.add(rerunQueryAfterRefresh);
        
        return additionalWidget;
    }

    @Override
    public RefreshingSelectionTablesSettings getResult() {
        return new RefreshingSelectionTablesSettings(refreshAutomatically.getValue(),
                                                     refreshIntervalInSeconds.getValue() == null ? null : refreshIntervalInSeconds.getValue() * 1000,
                                                     rerunQueryAfterRefresh.getValue());
    }

    @Override
    public Validator<RefreshingSelectionTablesSettings> getValidator() {
        return new Validator<RefreshingSelectionTablesSettings>() {
            @Override
            public String getErrorMessage(RefreshingSelectionTablesSettings valueToValidate) {
                Integer refreshInterval = valueToValidate.getRefreshIntervalInMilliseconds();
                
                if (refreshInterval == null) {
                    return stringMessages.refreshIntervalMustntBeEmpty();
                }
                if (refreshInterval <= 0) {
                    return stringMessages.refreshIntervalMustBeGreaterThanXSeconds("0");
                }
                
                return null;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return refreshIntervalInSeconds;
    }

}
