package com.sap.sailing.gwt.home.mobile.partials.liveraces;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.event.LiveRaceDTO;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatusRace;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatusResources;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase.Presenter;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

public class RegattaLiveRaces extends Composite implements RefreshableWidget<SortedSetResult<LiveRaceDTO>> {

    private static RegattaLiveRacesUiBinder uiBinder = GWT.create(RegattaLiveRacesUiBinder.class);
    
    interface RegattaLiveRacesUiBinder extends UiBinder<MobileSection, RegattaLiveRaces> {
    }

    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection mobileSection;
    private final Presenter presenter;

    public RegattaLiveRaces(final Presenter presenter) {
        this.presenter = presenter;
        RegattaStatusResources.INSTANCE.css().ensureInjected();
        initWidget(mobileSection = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.liveNow());
        setVisible(false);
    }
    
    @Override
    public void setData(SortedSetResult<LiveRaceDTO> data) {
        setVisible(data != null && !data.isEmpty());
        mobileSection.clearContent();
        if (data != null) {
            for (LiveRaceDTO liveRace : data.getValues()) {
                mobileSection.addContent(new RegattaStatusRace(liveRace, presenter::getRaceViewerURL));
            }
        }
    }
    
}
