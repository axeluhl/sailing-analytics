package com.sap.sailing.gwt.ui.client.shared.perspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;

public class PerspectiveConfigurationComposite extends Composite {
    @UiField Label nameUi;
    @UiField HTMLPanel perspectiveConfigurationPanel;
    
    interface PerspectiveConfigurationCompositeUiBinder extends UiBinder<Widget, PerspectiveConfigurationComposite> {
    }
    
    private static PerspectiveConfigurationCompositeUiBinder uiBinder = GWT.create(PerspectiveConfigurationCompositeUiBinder.class);

    private final Perspective perspective;
    
    public PerspectiveConfigurationComposite(Perspective perspective) {
        this.perspective = perspective;
        
        PerspectiveConfigurationCompositeResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));
        
        nameUi.setText(this.perspective.getPerspectiveName());
        
        for(final Component<?> component: perspective.getComponents()) {
            final Button settingsButton = new Button(component.getLocalizedShortName());
            settingsButton.setStyleName(PerspectiveConfigurationCompositeResources.INSTANCE.css().someClass());
            
            settingsButton.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    SettingsDialog settingsDialog = new SettingsDialog(component, StringMessages.INSTANCE);
                    settingsDialog.show();
                }
            });
            perspectiveConfigurationPanel.add(settingsButton);
        }
    }

}
