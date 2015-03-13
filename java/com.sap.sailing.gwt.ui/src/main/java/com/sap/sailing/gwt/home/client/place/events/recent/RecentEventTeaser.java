package com.sap.sailing.gwt.home.client.place.events.recent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.shared.event.EventTeaser;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public class RecentEventTeaser extends Composite {

    private static RecentEventTeaserUiBinder uiBinder = GWT.create(RecentEventTeaserUiBinder.class);

    interface RecentEventTeaserUiBinder extends UiBinder<Widget, RecentEventTeaser> {
    }
    
    @UiField(provided = true) EventTeaser eventTeaser;

    public RecentEventTeaser(final PlaceNavigation<?> placeNavigation, final EventMetadataDTO event) {
        eventTeaser = new EventTeaser(placeNavigation, event);
        initWidget(uiBinder.createAndBindUi(this));
    }

}
