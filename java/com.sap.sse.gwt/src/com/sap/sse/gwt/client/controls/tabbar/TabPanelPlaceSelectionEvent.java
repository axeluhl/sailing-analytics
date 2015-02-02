package com.sap.sse.gwt.client.controls.tabbar;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by pgtaboada on 01.12.14.
 */
public class TabPanelPlaceSelectionEvent<CONTEXT> extends GwtEvent<TabPanelPlaceSelectionEvent.Handler> {

    public final static Type<Handler> TYPE = new Type<Handler>();

    private TabActivity<?, CONTEXT> selectedActivity;

    public TabPanelPlaceSelectionEvent(TabActivity<?, CONTEXT> selectedActivity) {
        this.selectedActivity = selectedActivity;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    public TabActivity<?, CONTEXT> getSelectedActivity() {
        return selectedActivity;
    }

    @Override
    protected void dispatch(Handler handler) {

        handler.onTabSelected(this);
    }

    public interface Handler extends EventHandler {

        void onTabSelected(TabPanelPlaceSelectionEvent<?> event);

    }

}
