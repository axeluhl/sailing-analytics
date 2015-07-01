package com.sap.sailing.gwt.home.shared;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
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
    private Logger LOG = Logger.getLogger(SwitchingEntryPoint.class.getName());
    private static final String SAPSAILING_MOBILE = "sapsailing_mobile";
    @Override
    public void onModuleLoad() {
        String startMobile = Cookies.getCookie(SAPSAILING_MOBILE);
        if (startMobile != null) {
            // use user defined preferences
            if (Boolean.valueOf(startMobile)) {
                LOG.info("Switching to mobile by stored cookie");
                startMobile();
            } else {
                LOG.info("Switching to desktop by stored cookie");
                startDesktop();
            }
        } else {
            RegExp regExp = RegExp.compile("Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini", "i");
            // if( .test(navigator.userAgent) ) 
            LOG.info("User agent: " + Navigator.getUserAgent());
            boolean matches = regExp.test(Navigator.getUserAgent());
            LOG.info("Matches?" + matches);
            if (matches) {
                LOG.info("Identified mobile browser by user agent");
                startMobile();
            } else {
                LOG.info("Using desktop browser");
                startDesktop();
            }
        }
        
    }
    
    public static void switchToDesktop() {
        Cookies.setCookie(SAPSAILING_MOBILE, "false");
        Window.Location.reload();
    }

    public static void switchToMobile() {
        Cookies.setCookie(SAPSAILING_MOBILE, "true");
        Window.Location.reload();
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