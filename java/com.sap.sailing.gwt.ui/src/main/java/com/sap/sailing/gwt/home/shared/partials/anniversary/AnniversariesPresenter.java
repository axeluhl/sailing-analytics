package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.sap.sailing.gwt.home.communication.anniversary.AnniversaryDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class AnniversariesPresenter {

    private static final String COUNTDOWN_MAJOR_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String COUNTDOWN_REPDIGIT_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String ANNOUNCEMENT_ICON = "images/mobile/icon_trackedCount.svg";

    private AnniversariesView view;

    public AnniversariesPresenter(AnniversariesView view) {
        this.view = view;
    }

    public void addAnniversary(AnniversaryDTO anniversary) {
        final int countdown = anniversary.getCountDown(), target = anniversary.getTarget();
        final String teaser = StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(countdown, target);
        final String description = StringMessages.INSTANCE.anniversaryMajorCountdownDescription(target);
        this.view.addAnniversary(COUNTDOWN_MAJOR_ICON, teaser, description);
    }

}
