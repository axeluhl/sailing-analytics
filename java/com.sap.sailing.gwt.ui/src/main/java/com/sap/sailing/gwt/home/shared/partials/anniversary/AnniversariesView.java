package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnniversariesView extends IsWidget {

    void clearAnniversaries();

    void addCountdown(int countdown, String teaser, String description);

    void addAnnouncement(String iconUrl, int target, String teaser, String description, String linkUrl);
}
