package com.sap.sailing.dashboards.gwt.client.bottomnotification;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;

/**
 * This class is a notification that pops up from the bottom of the screen. In the dashboard it is used to tell the user
 * that there happened something important in the app. I.e if there is a new start analysis available.
 * 
 * @author Alexander Ries
 * 
 */
public class BottomNotification extends FocusPanel {

    private boolean shown;
    private Timer timer;
    private List<BottomNotificationClickListener> bottomNotificationClickListeners;

    public BottomNotification() {

        this.addStyleName("bottomnotification");
        this.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (shown == true) {
                    shown = false;
                    BottomNotification.this.getElement().getStyle().setOpacity(1.0);
                    nofitifyBottomNotificationClickListenersAboutClick();
                    hide();
                }
            }
        });
        this.addStyleName("bottomnotification");
        bottomNotificationClickListeners = new ArrayList<BottomNotificationClickListener>();
    }

    public void initClickHander() {

    }

    public void show(BottomNotificationShowOptions bottomNotificationShowOptions) {
        shown = true;
        this.removeStyleName("bottomnotificationhidden");
        this.addStyleName("bottomnotificationshow");
        this.getElement().setInnerHTML(bottomNotificationShowOptions.getMessage());
        this.getElement().getStyle().setBackgroundColor(bottomNotificationShowOptions.getBackgroundColorAsHex());
        this.getElement().getStyle().setColor(bottomNotificationShowOptions.getTextColorAsHex());
        if (bottomNotificationShowOptions.isShouldDisappearAfter20Seconds()) {
            Timer timer = new Timer() {
                public void run() {
                    hide();
                }
            };
            timer.schedule(20000);
        }
    }

    public void hide() {
        if (timer != null) {
            timer.cancel();
        }
        shown = false;
        this.removeStyleName("bottomnotificationshow");
        this.addStyleName("bottomnotificationhidden");
        this.getElement().setInnerHTML("");
    }

    public void addBottomNotificationClickListener(BottomNotificationClickListener bottomNotificationClickListener) {
        bottomNotificationClickListeners.add(bottomNotificationClickListener);
    }

    public void removeBottomNotificationClickListener(BottomNotificationClickListener bottomNotificationClickListener) {
        bottomNotificationClickListeners.remove(bottomNotificationClickListener);
    }

    private void nofitifyBottomNotificationClickListenersAboutClick() {
        for (BottomNotificationClickListener bncl : bottomNotificationClickListeners) {
            bncl.bottomNotificationClicked();
        }
    }
}
