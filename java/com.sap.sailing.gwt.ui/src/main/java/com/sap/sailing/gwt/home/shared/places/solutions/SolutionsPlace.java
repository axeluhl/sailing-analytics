package com.sap.sailing.gwt.home.shared.places.solutions;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SolutionsPlace extends AbstractBasePlace implements HasLocationTitle, HasMobileVersion {

    public enum SolutionsNavigationTabs {
        SapInSailing, SailingAnalytics, RaceManagerApp, InSightApp, BuoyPingerApp, PostRaceAnalytics, SailingSimulator
    };
    private final SolutionsNavigationTabs navigationTab;
    private final static String PARAM_NAVIGATION_TAB = "navigationTab";
    private final static String CALENDAR_ACCESS_COUNT_URL = "/gwt/Calendar.html";
    private final boolean invokedFromCalendar;

    public SolutionsPlace(String url) {
        super(url.split("\\?")[0]); // if the fragment holds another "?" check if it's the "Calendar marker" c=1
        final String[] splitUrl = url.split("\\?");
        if (splitUrl.length > 1) {
            final String calendarMarker = splitUrl[1];
            invokedFromCalendar = calendarMarker.equals("c=1");
        } else {
            invokedFromCalendar = false;
        }
        if (invokedFromCalendar) {
            navigationTab = SolutionsNavigationTabs.SapInSailing;
            makeLoggedCountableCalendarRequest();
        } else {
            final String paramNavTab = getParameter(PARAM_NAVIGATION_TAB);
            SolutionsNavigationTabs preliminaryNavTab;
            try {
                preliminaryNavTab = paramNavTab != null ? SolutionsNavigationTabs.valueOf(paramNavTab) : null;
            } catch (IllegalArgumentException e) {
                preliminaryNavTab = SolutionsNavigationTabs.SapInSailing;
            }
            navigationTab = preliminaryNavTab;
        }
    }
    
    private void makeLoggedCountableCalendarRequest() {
        final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, CALENDAR_ACCESS_COUNT_URL);
        try {
            requestBuilder.sendRequest(/* requestData */ null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    // nothing to do; it's just to make calendar-based access server-countable
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    // nothing to do; it's just to make calendar-based access server-countable; it failed...
                    GWT.log("Warning: couldn't access calendar link counter", exception);
                }
            });
        } catch (RequestException e) {
            GWT.log("Warning: couldn't access calendar link counter", e);
        }
    }

    public SolutionsPlace(SolutionsNavigationTabs navigationTab) {
        super(PARAM_NAVIGATION_TAB, navigationTab.name());
        this.navigationTab = navigationTab;
        this.invokedFromCalendar = false;
    }

    public SolutionsNavigationTabs getNavigationTab() {
        return navigationTab;
    }
    
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.solutions();
    }

    public static class Tokenizer implements PlaceTokenizer<SolutionsPlace> {
        @Override
        public String getToken(SolutionsPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public SolutionsPlace getPlace(String url) {
            return new SolutionsPlace(url);
        }
    }
}
