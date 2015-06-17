package com.sap.sailing.gwt.home.mobile.partials.regattaStatus;

import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.toggleButton.ToggleButton;
import com.sap.sailing.gwt.home.mobile.partials.toggleButton.ToggleButton.ToggleButtonCommand;
import com.sap.sailing.gwt.home.mobile.places.event.EventView.Presenter;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RegattasAndLiveRacesDTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public class RegattaStatus extends Composite implements RefreshableWidget<RegattasAndLiveRacesDTO> {

    private static final String TOGGLECONTAINERHIDDEN_STYLE = RegattaStatusResources.INSTANCE.css().togglecontainerhidden();
    private static RegattaStatusUiBinder uiBinder = GWT.create(RegattaStatusUiBinder.class);

    interface RegattaStatusUiBinder extends UiBinder<Widget, RegattaStatus> {
    }

    @UiField MobileSection itemContainerUi;
    @UiField FlowPanel regattaContainerUi;
    @UiField FlowPanel collapsableContainerUi;
    @UiField(provided = true) ToggleButton toggleButtonUi;
    private final Presenter presenter;
    
    public RegattaStatus(Presenter presenter) {
        this.presenter = presenter;
        RegattaStatusResources.INSTANCE.css().ensureInjected();
        toggleButtonUi = new ToggleButton(new ToggleButtonCommand() {
            @Override
            protected void execute(boolean expanded) {
                collapsableContainerUi.setStyleName(TOGGLECONTAINERHIDDEN_STYLE, !expanded);
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setData(RegattasAndLiveRacesDTO data, long nextUpdate, int updateNo) {
        regattaContainerUi.clear();
        collapsableContainerUi.clear();
        if (data.hasRegattasWithRaces()) {
            for (Entry<RegattaMetadataDTO, Set<LiveRaceDTO>> pair : data.getRegattasWithRaces().entrySet()) {
                RegattaStatusRegatta regattaWidget = addRegatta(regattaContainerUi, pair.getKey());
                for (LiveRaceDTO race : pair.getValue()) {
                    regattaWidget.addRace(race);
                }
            }
            for (RegattaMetadataDTO regatta : data.getRegattasWithoutRaces()) {
                addRegatta(collapsableContainerUi, regatta);
            }
        } else {
            for (RegattaMetadataDTO regatta : data.getRegattasWithoutRaces()) {
                addRegatta(regattaContainerUi, regatta);
            }
            collapsableContainerUi.removeFromParent();
            toggleButtonUi.removeFromParent();
        }
    }
    
    private RegattaStatusRegatta addRegatta(FlowPanel container, RegattaMetadataDTO regatta) {
        PlaceNavigation<?> placeNavigation = presenter.getRegattaLeaderboardNavigation(regatta.getId());
        RegattaStatusRegatta regattaWidget = new RegattaStatusRegatta(regatta, placeNavigation);
        container.add(regattaWidget);
        return regattaWidget;
    }
    
}
