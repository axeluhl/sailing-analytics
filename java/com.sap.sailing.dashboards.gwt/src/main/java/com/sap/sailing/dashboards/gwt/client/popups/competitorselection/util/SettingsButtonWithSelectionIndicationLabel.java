package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.util;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.dashboardpanel.DashboardPanelResources;

public class SettingsButtonWithSelectionIndicationLabel extends Composite implements HasWidgets {

    private static SettingsButtonWithSelectionIndicationLabelUiBinder uiBinder = GWT
            .create(SettingsButtonWithSelectionIndicationLabelUiBinder.class);

    interface SettingsButtonWithSelectionIndicationLabelUiBinder extends
            UiBinder<Widget, SettingsButtonWithSelectionIndicationLabel> {
    }

    interface SettingsButtonWithSelectionIndicationLabelStyle extends CssResource {
    }

    @UiField
    FocusPanel settingsButton;

    @UiField
    Image settingsButtonImage;
    
    @UiField
    HTMLPanel settingsIndicationLabel;

    public SettingsButtonWithSelectionIndicationLabel() {
        SettingsButtonWithSelectionIndicationLabelResources.INSTANCE.gss().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        settingsButtonImage.setResource(DashboardPanelResources.INSTANCE.settings());        
    }
    
    public void addClickHandlerToSettingsButton(ClickHandler clickHandler){
        settingsButton.addClickHandler(clickHandler);
    }

    public void setSelectionIndicationTextOnLabel(String selectionIndicationText) {
        settingsIndicationLabel.getElement().setInnerHTML(""+selectionIndicationText);
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
