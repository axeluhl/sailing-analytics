package com.sap.sailing.gwt.home.shared;

import static com.sap.sse.gwt.client.formfactor.MetaTagUtil.SCALE_VALUE_LARGE;
import static com.sap.sse.gwt.client.formfactor.MetaTagUtil.SCALE_VALUE_NORMAL;
import static com.sap.sse.gwt.client.formfactor.MetaTagUtil.SCALE_VALUE_SMALL;
import static com.sap.sse.gwt.client.formfactor.MetaTagUtil.SCALE_VALUE_SMALLER;
import static com.sap.sse.gwt.client.formfactor.MetaTagUtil.setViewportToDeviceWidth;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.desktop.DesktopEntryPoint;
import com.sap.sailing.gwt.home.mobile.MobileEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.ApplicationPlaceUpdater;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.app.MobileSupport;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;

/**
 * Wrapper EntryPoint for the Home module that decides if the desktop or mobile page will be shown and internally starts
 * the respective EntryPoint.
 * 
 * The decision is made on the following aspects:
 * <ul>
 * <li>the type of device (desktop, phone, table)</li>
 * <li>the place to be shown and if the place is available for one or the other form factor</li>
 * <li>the user preference that is being triggered by the user clicking "desktop version" or "mobile version" links in
 * the page footer</li>
 * </ul>
 */
public class SwitchingEntryPoint implements EntryPoint {
    private static Logger LOG = Logger.getLogger(SwitchingEntryPoint.class.getName());
    private static final String SAPSAILING_MOBILE = "sapsailing_mobile";

    private final PlaceHistoryMapper hisMap = GWT.create(ApplicationHistoryMapper.class);
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    @Override
    public void onModuleLoad() {
        Document.get().getElementById("loading").removeFromParent();
        LOG.info("Start switching entry point");

        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();

        String hash = Window.Location.getHash();
        if (hash != null && hash.startsWith("#")) {
            hash = hash.substring(1);
        }
        Place rawPlace = hisMap.getPlace(hash);
        Place place = placeUpdater.getRealPlace(rawPlace);
        String userWantsMobileUi = Cookies.getCookie(SAPSAILING_MOBILE);
        if (place != null && !hasMobileVersion(place)) {
            LOG.info("We have a dedicated desktop place: " + hash);
            startDesktop();
        } else if (userWantsMobileUi != null) {
            // use user defined preferences
            if (Boolean.parseBoolean(userWantsMobileUi)) {
                LOG.info("Switching to mobile by stored cookie");
                startMobile();
            } else {
                LOG.info("Switching to desktop by stored cookie");
                startDesktop();
            }
        } else {
            if (DeviceDetector.isMobile()) {
                LOG.info("Identified mobile (smartphone) browser by user agent");
                startMobile();
            } else {
                LOG.info("Using desktop version");
                startDesktop();
            }
        }
    }

    /**
     * Checks if the given {@link Place} has a mobile view which is indicated either by the {@link HasMobileVersion}
     * interface or the {@link MobileSupport#hasMobileVersion()} method.
     * 
     * @param place
     *            {@link Place} to check
     * @return <code>true</code> if the given {@link Place} implements {@link HasMobileVersion} interface or the
     *         implemented {@link MobileSupport#hasMobileVersion()} method returns <code>true</code>, <code>false</code>
     *         otherwise
     */
    public static boolean hasMobileVersion(Place place) {
        return place instanceof HasMobileVersion
                || (place instanceof MobileSupport && ((MobileSupport) place).hasMobileVersion());
    }

    public static boolean viewIsLockedToDesktop() {
        String userWantsMobileUi = Cookies.getCookie(SAPSAILING_MOBILE);
        return (userWantsMobileUi != null && !Boolean.parseBoolean(userWantsMobileUi));
    }

    public static void switchToDesktop() {
        if (DeviceDetector.isDesktop()) {
            Cookies.removeCookie(SAPSAILING_MOBILE);
        } else {
            // only force "not mobile" on mobile devices
            Cookies.setCookie(SAPSAILING_MOBILE, "false");
        }
        reloadApp();
    }

    public static void switchToMobile() {
        if (DeviceDetector.isMobile()) {
            Cookies.removeCookie(SAPSAILING_MOBILE);
        } else {
            // only force mobile version on desktop
            Cookies.setCookie(SAPSAILING_MOBILE, "true");
        }
        reloadApp();
    }

    private void startMobile() {
        configureMobileHeader();
        new MobileEntryPoint().onModuleLoad();
    }

    private void startDesktop() {
        configureDesktopHeader();
        new DesktopEntryPoint().onModuleLoad();
    }

    private void configureDesktopHeader() {
        setViewportToDeviceWidth(DeviceDetector.isTablet() ? SCALE_VALUE_SMALLER : SCALE_VALUE_SMALL,
                SCALE_VALUE_LARGE);
    }

    private void configureMobileHeader() {
        setViewportToDeviceWidth(SCALE_VALUE_NORMAL, SCALE_VALUE_NORMAL);
    }

    public static void reloadApp() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                Window.Location.reload();
            }
        });
    }

}
