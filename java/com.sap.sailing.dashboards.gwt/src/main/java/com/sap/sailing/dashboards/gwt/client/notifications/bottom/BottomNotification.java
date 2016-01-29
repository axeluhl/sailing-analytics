package com.sap.sailing.dashboards.gwt.client.notifications.bottom;

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
        BottomNotificationResources.INSTANCE.gss().ensureInjected();
        this.addStyleName(BottomNotificationResources.INSTANCE.gss().notification());
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
            this.removeStyleName(BottomNotificationResources.INSTANCE.gss().hidden());
            this.addStyleName(BottomNotificationResources.INSTANCE.gss().shown());
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
        this.removeStyleName(BottomNotificationResources.INSTANCE.gss().shown());
        this.addStyleName(BottomNotificationResources.INSTANCE.gss().hidden());
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
