package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.util;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.widgets.ActionPanel;
import com.sap.sailing.dashboards.gwt.client.widgets.ActionPanel.ActionPanelListener;

public class SettingsButtonWithSelectionIndicationLabel extends Composite implements HasWidgets {

    private static SettingsButtonWithSelectionIndicationLabelUiBinder uiBinder = GWT
            .create(SettingsButtonWithSelectionIndicationLabelUiBinder.class);

    interface SettingsButtonWithSelectionIndicationLabelUiBinder extends
            UiBinder<Widget, SettingsButtonWithSelectionIndicationLabel> {
    }

    interface SettingsButtonWithSelectionIndicationLabelStyle extends CssResource {
    }

    @UiField(provided = true)
    ActionPanel settingsButton;

    @UiField
    Image settingsButtonImage;

    @UiField
    HTMLPanel settingsIndicationLabel;

    public SettingsButtonWithSelectionIndicationLabel() {
        SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.gss().ensureInjected();
        settingsButton = new ActionPanel(Event.TOUCHEVENTS, Event.ONCLICK);
        initWidget(uiBinder.createAndBindUi(this));
        settingsButtonImage.setResource(SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.settings());
    }
    
    public void addActionListener(ActionPanelListener actionPanelListener) {
        settingsButton.addActionPanelListener(actionPanelListener);
    }

    public void setSelectionIndicationTextOnLabel(String selectionIndicationText) {
        settingsIndicationLabel.getElement().setInnerHTML("" + selectionIndicationText);
    }

    public void disable() {
        settingsButton.disable();
        settingsButtonImage.setResource(SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.settingsDisabled());
        settingsIndicationLabel.getElement().addClassName(SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.gss().settings_indication_label_disabled());
    }

    public void enable() {
        settingsButton.enable();
        settingsButtonImage.setResource(SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.settings());
        settingsIndicationLabel.getElement().removeClassName(SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.gss().settings_indication_label_disabled());
    }
    
    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}
