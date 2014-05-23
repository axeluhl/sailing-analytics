package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.impl.ActivityMapperRegistry;

/**
 * Subclasses implement their {@link EntryPoint#onModuleLoad()} by delegating to
 * {@link #onModuleLoad(AcceptsOneWidget, Widget, Place, ClientFactory, Class, ActivityMapper...)},
 * providing several configuration objects for the entry point.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractEntryPoint implements EntryPoint {

    /**
     * Initializes the application with a {@link PlaceHistoryHandler} using as a {@link PlaceHistoryMapper} what it gets
     * by passing <code>placeHistoryMapperClass</code> to a {@link GWT#create(Class)} call. An {@link ActivityManager}
     * is configured using a composite {@link ActivityMapperRegistry activity mapper} considering all the
     * <code>activityMappers</code> passed to this method. As display for the activities' views the activity manager
     * uses the <code>stage</code> which is assumed to be or to be contained in the <code>rootWidget</code> which is
     * finally set as the entry point's root panel content element. Ultimately, the history handler is asked to navigate
     * to the place identified by the URL or to the <code>defaultPlace</code>.
     * <p>
     * 
     * @param stage
     *            the stage where to display the activity views as places are changed
     * @param rootWidget
     *            expected to contain the <code>stage</code>; set as the root panel content element
     * @param defaultPlace
     *            navigates to this place if the URL doesn't specify any other place
     * @param clientFactory
     *            used to determine the event bus and the place controller.
     * @param placeHistoryMapperClass
     *            used to create a place history mapper with {@link GWT#create(Class)}
     * @param activityMappers
     *            used for a composite activity mapper; the first mapper to provide an activity for a place gets its way
     */
    public void onModuleLoad(AcceptsOneWidget stage, Widget rootWidget, Place defaultPlace,
            ClientFactory clientFactory, Class<? extends PlaceHistoryMapper> placeHistoryMapperClass,
            ActivityMapper... activityMappers) {
        // Start ActivityManager for the main widget with our ActivityMapper
        ActivityMapperRegistry activityMapperRegistry = new ActivityMapperRegistry();
        for (ActivityMapper activityMapper : activityMappers) {
            activityMapperRegistry.addActivityMapper(activityMapper);
        }
        EventBus eventBus = clientFactory.getEventBus();
        ActivityManager activityManager = new ActivityManager(activityMapperRegistry, eventBus);
        activityManager.setDisplay(stage);

        // Start PlaceHistoryHandler with our PlaceHistoryMapper
        PlaceHistoryMapper historyMapper = GWT.create(placeHistoryMapperClass);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(clientFactory.getPlaceController(), eventBus, defaultPlace);

        RootPanel.get().add(rootWidget);
        // Goes to place represented on URL or default place
        historyHandler.handleCurrentHistory();
    }
}
