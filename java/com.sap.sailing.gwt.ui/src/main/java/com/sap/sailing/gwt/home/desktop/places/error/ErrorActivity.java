package com.sap.sailing.gwt.home.desktop.places.error;

import com.google.gwt.user.client.Command;
import com.sap.sailing.gwt.home.shared.places.error.AbstractErrorActivity;
import com.sap.sailing.gwt.home.shared.places.error.ErrorClientFactory;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class ErrorActivity extends AbstractErrorActivity {

    public ErrorActivity(ErrorPlace place, ErrorClientFactory clientFactory) {
        super(place, clientFactory.getPlaceController());
    }
    
    @Override
    protected ErrorView createView(String errorMsg, Throwable reason, Command reloadCommand) {
        return new TabletAndDesktopErrorView(errorMsg, reason, reloadCommand);
    }

    @Override
    protected ErrorView createView(String customMsg, String errorMsg, Throwable reason, Command reloadCommand) {
        return new TabletAndDesktopErrorView(customMsg, errorMsg, reason, reloadCommand);
    }

}
