package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.sap.sailing.gwt.home.shared.partials.anniversary.AbstractAnniversaries;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView;

/**
 * {@link AnniversariesView} implementation for desktop.
 */
public class DesktopAnniversaries extends AbstractAnniversaries implements AnniversariesView {

    @Override
    public AnniversaryAnnouncement addAnnouncement() {
        return addAnniversaryItem(new AnniversaryItem(true));
    }

    @Override
    public AnniversaryCountdown addCountdown() {
        return addAnniversaryItem(new AnniversaryItem(false));
    }

}
