package com.sap.sailing.gwt.home.client.place.event.partials.countdownTimer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface CountdownTimerResources extends ClientBundle {
    public static final CountdownTimerResources INSTANCE = GWT.create(CountdownTimerResources.class);

    @Source("CountdownTimer.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String countdowntimer();
        String countdowntimer_content();
        String countdowntimer_content_panewrapper();
        String countdowntimer_content_pane();
        String countdowntimer_content_panefirst();
        String countdowntimer_content_panesecond();
        String countdowntimer_content_panethird();
        String countdowntimer_content_panefourth();
        String countdowntimer_content_pane_count();
        String countdowntimer_content_pane_unit();
    }
}
