package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardImageResources;

public class SettingsButtonWithSelectionIndicationLabel extends Composite {

    private static SettingsButtonWithSelectionIndicationLabelUiBinder uiBinder = GWT
            .create(SettingsButtonWithSelectionIndicationLabelUiBinder.class);

    interface SettingsButtonWithSelectionIndicationLabelUiBinder extends
            UiBinder<Widget, SettingsButtonWithSelectionIndicationLabel> {
    }

    interface SettingsButtonWithSelectionIndicationLabelStyle extends CssResource {
    }

    @UiField
    SettingsButtonWithSelectionIndicationLabelStyle style;

    @UiField
    FocusPanel settingsButton;

    @UiField
    Image settingsButtonImage;
    
    @UiField
    Label settingsIndicationLabel;

    public SettingsButtonWithSelectionIndicationLabel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        settingsButtonImage.setResource(RibDashboardImageResources.INSTANCE.settings());
        
//        settingsButton.addDomHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                Window.alert("OK?");
//            }
//        }, ClickEvent.getType());
//        
//        settingsButton.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                Window.alert("OK?");
//            }
//        });
        
    }
    
    public void addClickHandlerToSettingsButton(ClickHandler clickHandler){
        settingsButton.addClickHandler(clickHandler);
    }

    public void setSelectionIndicationTextOnLabel(String selectionIndicationText) {
        settingsIndicationLabel.setText(selectionIndicationText);
    }
}
