package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.sap.sailing.domain.common.dto.AnniversaryType;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversariesDTO;
import com.sap.sailing.gwt.home.communication.anniversary.AnniversaryDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class AnniversariesPresenter implements RefreshableWidget<AnniversariesDTO> {

    private static final String COUNTDOWN_MAJOR_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String COUNTDOWN_REPDIGIT_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String ANNOUNCEMENT_ICON = "images/mobile/icon_trackedCount.svg";

    private AnniversariesView view;

    public AnniversariesPresenter(AnniversariesView view) {
        this.view = view;
    }

    @Override
    public void setData(AnniversariesDTO data) {
        this.view.clearAnniversaries();
        this.view.asWidget().setVisible(!data.isEmpty());
        data.getValues().forEach(this::addAnniversary);
    }

    private void addAnniversary(AnniversaryDTO anniversary) {
        final int countdown = anniversary.getCountDown(), target = anniversary.getTarget();
        final String iconUrl, teaser, description;
        if (anniversary.isAnnouncement()) {
            iconUrl = ANNOUNCEMENT_ICON;
            teaser = StringMessages.INSTANCE.anniversaryAnnouncementTeaser(target);
            String raceDisplayName = anniversary.getRaceName() + ", " + anniversary.getLeaderBoardName();
            description = StringMessages.INSTANCE.anniversaryAnnouncementDescription(raceDisplayName);
        } else if (anniversary.getType() == AnniversaryType.QUARTER) {
            iconUrl = COUNTDOWN_MAJOR_ICON;
            teaser = StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(countdown, target);
            description = StringMessages.INSTANCE.anniversaryMajorCountdownDescription(target);
        } else if (anniversary.getType() == AnniversaryType.REPEATED_DIGIT) {
            iconUrl = COUNTDOWN_REPDIGIT_ICON;
            teaser = StringMessages.INSTANCE.anniversaryRepdigitCountdownTeaser(countdown, target);
            description = StringMessages.INSTANCE.anniversaryRepdigitCountdownDescription(target);
        } else {
            return;
        }
        this.view.addAnniversary(iconUrl, teaser, description);
    }

}
