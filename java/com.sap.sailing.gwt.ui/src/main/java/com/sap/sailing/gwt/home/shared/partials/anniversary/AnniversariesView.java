package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.user.client.ui.IsWidget;

public interface AnniversariesView extends IsWidget {

    void clearAnniversaries();

    void addAnniversary(String iconUrl, String teaser, String description);
}
