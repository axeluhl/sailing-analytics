package com.sap.sailing.dashboards.gwt.client.notifications;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
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

    private static NotificationsResources resources = GWT.create(NotificationsResources.class);
    
    public BottomNotification() {
        resources.notificationsStyle().ensureInjected();
        this.addStyleName(resources.notificationsStyle().bottomnotification());
        this.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (shown == true) {
                    shown = false;
                    nofitifyBottomNotificationClickListenersAboutClick();
                    hide();
                }
            }
        });
        bottomNotificationClickListeners = new ArrayList<BottomNotificationClickListener>();
    }

    public void show(BottomNotificationType bottomNotificationType) {
        if (shown == false) {
            this.removeStyleName(resources.notificationsStyle().bottomnotification_hidden());
            this.addStyleName(resources.notificationsStyle().bottomnotification_shown());
            this.getElement().setInnerHTML(bottomNotificationType.getMessage());
            this.getElement().getStyle().setBackgroundColor(bottomNotificationType.getBackgroundColorAsHex());
            this.getElement().getStyle().setColor(bottomNotificationType.getTextColorAsHex());
            Timer timer = new Timer() {
                public void run() {
                    hide();
                }
            };
            timer.schedule(bottomNotificationType.timeToDisappearInMilliseconds());
            shown = true;
        }
    }

    public void hide() {
        if (timer != null) {
            timer.cancel();
        }
        this.removeStyleName(resources.notificationsStyle().bottomnotification_shown());
        this.addStyleName(resources.notificationsStyle().bottomnotification_hidden());
        this.getElement().setInnerHTML("");
        shown = false;
    }

    public void addBottomNotificationClickListener(BottomNotificationClickListener bottomNotificationClickListener) {
        bottomNotificationClickListeners.add(bottomNotificationClickListener);
    }

    public void removeBottomNotificationClickListener(BottomNotificationClickListener bottomNotificationClickListener) {
        bottomNotificationClickListeners.remove(bottomNotificationClickListener);
    }

    private void nofitifyBottomNotificationClickListenersAboutClick() {
        for (BottomNotificationClickListener bottomNotificationClickListener : bottomNotificationClickListeners) {
            bottomNotificationClickListener.bottomNotificationClicked();
        }
    }
}
