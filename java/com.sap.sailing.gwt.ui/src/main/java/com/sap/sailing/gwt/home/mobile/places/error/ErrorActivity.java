package com.sap.sailing.gwt.home.mobile.places.error;

import com.google.gwt.user.client.Command;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.error.AbstractErrorActivity;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class ErrorActivity extends AbstractErrorActivity {

    public ErrorActivity(ErrorPlace place, MobileApplicationClientFactory clientFactory) {
        super(place, clientFactory.getPlaceController());
    }

    @Override
    protected ErrorView createView(String errorMsg, Throwable reason, Command reloadCommand) {
        return new ErrorViewImpl(errorMsg, reason, reloadCommand);
    }

    @Override
    protected ErrorView createView(String customMsg, String errorMsg, Throwable reason, Command reloadCommand) {
        return new ErrorViewImpl(customMsg, errorMsg, reason, reloadCommand);
    }

}
