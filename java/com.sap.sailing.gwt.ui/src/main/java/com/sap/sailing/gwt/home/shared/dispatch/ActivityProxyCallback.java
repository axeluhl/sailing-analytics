package com.sap.sailing.gwt.home.shared.dispatch;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.gwt.client.mvp.ClientFactory;

/**
 * Convenience implementation of {@link AsyncCallback} interface to use in {@link AbstractActivityProxy} subclasses.
 * It implements {@link AsyncCallback#onFailure(Throwable)} method for a consistent error handling.
 */
public abstract class ActivityProxyCallback<T> implements AsyncCallback<T> {

    private final ClientFactory clientFactory;
    private final Place comingFromPlace;

    public ActivityProxyCallback(ClientFactory clientFactory, Place comingFromPlace) {
        this.clientFactory = clientFactory;
        this.comingFromPlace = comingFromPlace;
    }

    @Override
    public final void onFailure(Throwable caught) {
        // TODO @FM: extract text?
        ErrorPlace errorPlace = new ErrorPlace("Error while loading page: " + comingFromPlace.getClass().getSimpleName() + "!");
        // TODO @FM: reload sinnvoll hier?
        errorPlace.setComingFrom(comingFromPlace);
        clientFactory.getPlaceController().goTo(errorPlace);
    }

}
