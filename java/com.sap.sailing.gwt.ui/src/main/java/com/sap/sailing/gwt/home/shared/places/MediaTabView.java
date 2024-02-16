package com.sap.sailing.gwt.home.shared.places;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.ui.shared.ManageMediaModel;

public interface MediaTabView<PLACE extends Place, PRESENTER extends EventView.Presenter> extends TabView<PLACE, PRESENTER> {
    default TabView.State getState() {
        return getPresenter().hasMedia() || ManageMediaModel.hasEventMediaPermissions(getPresenter().getEventDTO(), getPresenter().getUserService())
                        ? TabView.State.VISIBLE
                        : TabView.State.INVISIBLE;
    }
}
