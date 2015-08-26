package com.sap.sailing.gwt.home.mobile.partials.liveraces;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.reload.RefreshableWidget;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatusRace;
import com.sap.sailing.gwt.home.mobile.partials.regattaStatus.RegattaStatusResources;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;

public class RegattaLiveRaces extends Composite implements RefreshableWidget<SortedSetResult<LiveRaceDTO>> {

    private final MobileSection mobileSection = new MobileSection();
    private final SectionHeaderContent header = new SectionHeaderContent();

    public RegattaLiveRaces() {
        RegattaStatusResources.INSTANCE.css().ensureInjected();
        header.setSectionTitle(StringMessages.INSTANCE.liveNow());
        mobileSection.addHeader(header);
        initWidget(mobileSection);
        setVisible(false);
    }
    
    @Override
    public void setData(SortedSetResult<LiveRaceDTO> data) {
        setVisible(!data.isEmpty());
        mobileSection.clearContent();
        for (LiveRaceDTO liveRace : data.getValues()) {
            mobileSection.addContent(new RegattaStatusRace(liveRace));
        }
    }
    
}
