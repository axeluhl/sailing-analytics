package com.sap.sailing.gwt.common.client.controls.tabbar;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by pgtaboada on 01.12.14.
 */
public class TabPanelPlaceSelectionEvent extends GwtEvent<TabPanelPlaceSelectionEvent.Handler> {

    public final static Type<Handler> TYPE = new Type<Handler>();

    private TabView<?, ?> selectedActivity;

    public TabPanelPlaceSelectionEvent(TabView<?, ?> selectedActivity) {
        this.selectedActivity = selectedActivity;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    public TabView<?, ?> getSelectedActivity() {
        return selectedActivity;
    }

    @Override
    protected void dispatch(Handler handler) {

        handler.onTabSelected(this);
    }

    public interface Handler extends EventHandler {

        void onTabSelected(TabPanelPlaceSelectionEvent event);

    }

}
