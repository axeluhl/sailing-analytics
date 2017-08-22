package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class AnniversariesPresenter {

    private static final String COUNTDOWN_MAJOR_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String COUNTDOWN_REPDIGIT_ICON = "images/mobile/icon_trackedCount.svg";
    private static final String ANNOUNCEMENT_ICON = "images/mobile/icon_trackedCount.svg";

    int countdown = 499, anniversary = 10000;

    String teaser = StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(countdown, anniversary);
    String description = StringMessages.INSTANCE.anniversaryMajorCountdownDescription(anniversary);

    private AnniversariesView view;

    public AnniversariesPresenter(AnniversariesView view) {
        this.view = view;
    }

    public void addAnniversary() {
        this.view.addAnniversary(COUNTDOWN_MAJOR_ICON, teaser, description);
    }

}
