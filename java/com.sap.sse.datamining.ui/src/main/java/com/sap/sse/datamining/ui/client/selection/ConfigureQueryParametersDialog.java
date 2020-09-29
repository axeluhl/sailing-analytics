package com.sap.sse.datamining.ui.client.selection;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ConfigureQueryParametersDialog extends AbstractDataMiningComponent<SerializableSettings> {
    
    private final DialogBox dialog;

    public ConfigureQueryParametersDialog(Component<?> parent, ComponentContext<?> componentContext) {
        super(parent, componentContext);

        dialog = new DialogBox(false, true);
        dialog.setText(getLocalizedShortName());
        dialog.setAnimationEnabled(true);
        
        DockPanel dialogPanel = new DockPanel();
        dialogPanel.setWidth("100%");
        dialog.setWidget(dialogPanel);
        
        // Dialog Content
        Panel contentPanel = new SimplePanel();
        contentPanel.add(new Label("I am a Dialog!"));
        
        // Dialog Buttons
        FlowPanel buttonsBar = new FlowPanel();
        Style buttonsBarStyle = buttonsBar.getElement().getStyle();
        buttonsBarStyle.setProperty("display", "flex");
        buttonsBarStyle.setProperty("justifyContent", "flex-end");
        
        Button closeButton = new Button(getDataMiningStringMessages().close());
        closeButton.addClickHandler((e) -> setVisible(false));
        buttonsBar.add(closeButton);
        
        // Final Layout
        dialogPanel.add(buttonsBar, DockPanel.SOUTH);
        dialogPanel.add(contentPanel, DockPanel.CENTER);
    }
    
    // TODO Parameter Overview
    // TODO Creation/Editing of dimension parameters
    // TODO Select parameter to be used as filter dimension values

    @Override
    public String getId() {
        return "ConfigureQueryParametersDialog";
    }

    @Override
    public String getLocalizedShortName() {
        return this.getDataMiningStringMessages().configureQueryParametersDialog();
    }

    @Override
    public Widget getEntryWidget() {
        return dialog;
    }

    @Override
    public boolean isVisible() {
        return dialog.isShowing();
    }

    @Override
    public void setVisible(boolean visibility) {
        if (visibility) {
            dialog.center();
        } else {
            dialog.hide();
        }
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(
            SerializableSettings useTheseSettings) {
        return null;
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) { }

    @Override
    public String getDependentCssClassName() {
        return "configure-query-parameters-dialog";
    }

}
