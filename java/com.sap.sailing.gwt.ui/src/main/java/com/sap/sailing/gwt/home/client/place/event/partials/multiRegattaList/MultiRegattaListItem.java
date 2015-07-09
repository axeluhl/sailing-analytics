package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.partials.regattaHeader.RegattaHeader;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class MultiRegattaListItem extends Composite {

    private static MultiRegattaListItemUiBinder uiBinder = GWT.create(MultiRegattaListItemUiBinder.class);

    interface MultiRegattaListItemUiBinder extends UiBinder<Widget, MultiRegattaListItem> {
    }
    
    @UiField(provided = true) RegattaHeader regattaHeaderUi;
    @UiField(provided = true) MultiRegattaListSteps regattaStepsUi;
    private final RegattaWithProgressDTO regattaWithProgress;

    public MultiRegattaListItem(RegattaWithProgressDTO regattaWithProgress, Presenter presenter) {
        this.regattaWithProgress = regattaWithProgress;
        regattaHeaderUi = new RegattaHeader(regattaWithProgress); 
        regattaStepsUi = new MultiRegattaListSteps(regattaWithProgress.getProgress());
        initWidget(uiBinder.createAndBindUi(this));
        regattaHeaderUi.setRegattaNavigation(presenter.getRegattaNavigation(regattaWithProgress.getId()));
        regattaStepsUi.setLeaderboardNavigation(presenter.getRegattaLeaderboardNavigation(regattaWithProgress.getId()));
    }
    
    void setVisibilityDependingOnBoatCategory(String visibleBoatCategory) {
        setVisible(visibleBoatCategory == null || visibleBoatCategory.equals(regattaWithProgress.getBoatCategory()));
    }

}
