package com.sap.sailing.gwt.home.shared;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.home.desktop.DesktopEntryPoint;
import com.sap.sailing.gwt.home.mobile.MobileEntryPoint;

public class SwitchingEntryPoint implements EntryPoint {
    private static Logger LOG = Logger.getLogger(SwitchingEntryPoint.class.getName());
    private static final String SAPSAILING_MOBILE = "sapsailing_mobile";
    private static final RegExp isMobileRegExp = RegExp.compile(
            "Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini", "i");

    @Override
    public void onModuleLoad() {
        String startMobileCookie = Cookies.getCookie(SAPSAILING_MOBILE);
        if (startMobileCookie != null) {
            // use user defined preferences
            if (Boolean.valueOf(startMobileCookie)) {
                LOG.info("Switching to mobile by stored cookie");
                startMobile();
            } else {
                LOG.info("Switching to desktop by stored cookie");
                startDesktop();
            }
        } else {
            if (isMobile()) {
                LOG.info("Identified mobile browser by user agent");
                startMobile();
            } else {
                LOG.info("Using desktop browser");
                startDesktop();
            }
        }
    }

    /**
     * Uses regular expression and user agent to detect mobile device.
     * 
     * @return
     */
    public static boolean isMobile() {
        boolean isMobile = isMobileRegExp.test(Navigator.getUserAgent());
        LOG.info("Navigator user agent matched mobile regex: " + isMobile);
        return isMobile;
    }

    /**
     * Convinience method
     * 
     * @return
     */
    public static boolean isDesktop() {
        return !isMobile();
    }

    public static void switchToDesktop() {
        if (isDesktop()) {
            Cookies.removeCookie(SAPSAILING_MOBILE);
        } else {
            // only force "not mobile" on mobile devices
            Cookies.setCookie(SAPSAILING_MOBILE, "false");
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                Window.Location.reload();
            }
        });
    }

    public static void switchToMobile() {
        if (isMobile()) {
            Cookies.removeCookie(SAPSAILING_MOBILE);
        } else {
            // only force mobile version on desktop
            Cookies.setCookie(SAPSAILING_MOBILE, "true");
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                Window.Location.reload();
            }
        });
    }

    private void startMobile() {
        configureMobileHeader();
        GWT.runAsync(MobileEntryPoint.class, new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                new MobileEntryPoint().onModuleLoad();
            }

            @Override
            public void onFailure(Throwable reason) {
                LOG.severe("Failed to async start mobile version: " + reason.getMessage());
                RootPanel.get().add(new Label("Failed to load mobile version"));
            }
        });
    }

    private void startDesktop() {
        configureDesktopHeader();
        GWT.runAsync(DesktopEntryPoint.class, new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                new DesktopEntryPoint().onModuleLoad();
            }

            @Override
            public void onFailure(Throwable reason) {
                LOG.severe("Failed to async start desktop version: " + reason.getMessage());
                RootPanel.get().add(new Label("Failed to load desktop version"));
            }
        });
    }

    private void configureDesktopHeader() {
        metaElement("viewport", "width=device-width,initial-scale=0.5,maximum-scale=1");
    }

    private void configureMobileHeader() {
        metaElement("viewport", "width=device-width,initial-scale=1,maximum-scale=1");
    }

    private void metaElement(String name, String content) {
        MetaElement metaElement = Document.get().createMetaElement();
        metaElement.setName(name);
        metaElement.setContent(content);
        Document.get().getHead().appendChild(metaElement);
    }
}