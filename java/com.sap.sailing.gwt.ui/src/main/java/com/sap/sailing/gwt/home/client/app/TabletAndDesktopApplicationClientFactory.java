package com.sap.sailing.gwt.home.client.app;


public class TabletAndDesktopApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public TabletAndDesktopApplicationClientFactory() {
        super(new TabletAndDesktopApplicationView());
    }
}
