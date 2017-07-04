package com.sap.sailing.gwt.home.desktop.partials.multiregattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.desktop.partials.regattaheader.RegattaHeader;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sse.common.Util;

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
        regattaHeaderUi.setRegattaRacesNavigation(presenter.getRegattaRacesNavigation(regattaWithProgress.getId()));
        PlaceNavigation<?> leaderboardNavigation = presenter.getRegattaLeaderboardNavigation(regattaWithProgress.getId());
        regattaStepsUi.setLeaderboardNavigation(regattaWithProgress.getState(), leaderboardNavigation);
    }
    
    void setVisibilityDependingOnLeaderboardGroup(String nameOfVisibleLeaderboardGroup) {
        setVisible(nameOfVisibleLeaderboardGroup == null || Util.contains(regattaWithProgress.getLeaderboardGroupNames(), nameOfVisibleLeaderboardGroup));
    }

}
