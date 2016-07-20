package com.sap.sailing.gwt.home.shared.partials.multiselection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;

class SuggestedMultiSelectionNotificationToggle extends Composite implements HasEnabled {

    private static LocalUiBinder uiBinder = GWT.create(LocalUiBinder.class);

    interface LocalUiBinder extends UiBinder<Widget, SuggestedMultiSelectionNotificationToggle> {
    }
    
    @UiField SharedResources res;
    @UiField Label labelUi;
    @UiField Button toggleButtonUi;
    private boolean enabled = false;
    
    SuggestedMultiSelectionNotificationToggle(String label) {
        initWidget(uiBinder.createAndBindUi(this));
        labelUi.setText(label);
    }
    
    @UiHandler("toggleButtonUi")
    void onToggleButtonClicked(ClickEvent event) {
        setEnabled(!isEnabled());
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updateUiState();
    }
    
    private void updateUiState() {
        toggleButtonUi.setStyleName(res.mainCss().buttonred(), !enabled);
        toggleButtonUi.setText(enabled ? StringMessages.INSTANCE.enabled() : StringMessages.INSTANCE.disabled());
    }
    
}
