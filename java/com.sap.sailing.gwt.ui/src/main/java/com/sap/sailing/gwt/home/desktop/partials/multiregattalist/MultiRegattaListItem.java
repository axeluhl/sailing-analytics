package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.partials.regattaheader.RegattaHeader;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class MultiRegattaListItem extends Composite {

    private static MultiRegattaListItemUiBinder uiBinder = GWT.create(MultiRegattaListItemUiBinder.class);

    interface MultiRegattaListItemUiBinder extends UiBinder<Widget, MultiRegattaListItem> {
    }
    
    @UiField(provided = true) RegattaHeader regattaHeaderUi;
    @UiField(provided = true) MultiRegattaListSteps regattaStepsUi;
    private final RegattaWithProgressDTO regattaWithProgress;
    
    public MultiRegattaListItem(RegattaWithProgressDTO regattaWithProgress, boolean showStateMarker) {
        this.regattaWithProgress = regattaWithProgress;
        regattaHeaderUi = new RegattaHeader(regattaWithProgress, showStateMarker); 
        regattaStepsUi = new MultiRegattaListSteps(regattaWithProgress.getProgress());
        initWidget(uiBinder.createAndBindUi(this));
    }

    public MultiRegattaListItem(RegattaWithProgressDTO regattaWithProgress, Presenter presenter) {
        this(regattaWithProgress, presenter.getEventDTO().isRunning());
        regattaHeaderUi.setRegattaNavigation(presenter.getRegattaNavigation(regattaWithProgress.getId()));
        PlaceNavigation<?> leaderboardNavigation = presenter.getRegattaLeaderboardNavigation(regattaWithProgress.getId());
        regattaStepsUi.setLeaderboardNavigation(regattaWithProgress.getState(), leaderboardNavigation);
    }
    
    void setVisibilityDependingOnBoatCategory(String visibleBoatCategory) {
        setVisible(visibleBoatCategory == null || visibleBoatCategory.equals(regattaWithProgress.getBoatCategory()));
    }

}
