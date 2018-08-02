package com.sap.sse.gwt.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * Utility class to show non obstructive warning / info messages using a small notification at the bottom of the page.
 * Clicking the notification will hide it. It will autohide after some time. To generate notifications use method
 * {@link com.sap.sse.gwt.client.Notification#notify()}.
 */
public class NotificationPanel {
    private static final int NOTIFICATION_TIME = 10000;
    private static final double FADE_OUT_PERCENT = 0.98;
    private static final double FADE_IN_PERCENT = 0.005;

    private final String message;
    private final Panel panel;
    private final Panel parent;
    private final Animation animation;

    private boolean alreadyShown = false;


    public NotificationPanel(String message, NotificationType type, Panel parent) {
        this.message = message;
        this.parent = parent;

        panel = new FlowPanel();
        panel.addStyleName(Notification.ress.css().notification());
        panel.getElement().getStyle().setCursor(Cursor.POINTER);

        panel.getElement().getStyle().setColor(type.getColor());
        panel.getElement().getStyle().setBackgroundColor(type.getBackgroundColor());
        panel.getElement().setInnerText(type.getDecorator() + " " + message);
        panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                animation.cancel();
            }
        }, ClickEvent.getType());

        animation = new Animation() {
            @Override
            protected void onStart() {
                super.onStart();
                alreadyShown = true;
                panel.getElement().getStyle().setOpacity(0);
            }

            @Override
            protected void onUpdate(double progress) {
                if (progress < FADE_IN_PERCENT) {
                    double relPr = progress / FADE_IN_PERCENT;
                    panel.getElement().getStyle().setOpacity(relPr);
                } else if (progress > FADE_OUT_PERCENT) {
                    double relPr = (progress - FADE_OUT_PERCENT) / (1 - FADE_OUT_PERCENT);
                    panel.getElement().getStyle().setOpacity(1 - relPr);
                } else {
                    panel.getElement().getStyle().setOpacity(1);
                }
            }

            @Override
            protected void onComplete() {
                remove();
            }

            @Override
            protected void onCancel() {
                remove();
            }
        };
    }
    
    /**
     * Displays notification at UI.
     */
    public void show() {
        if (!animation.isRunning()) {
            parent.add(panel);
            animation.run(NOTIFICATION_TIME);
        }
    }
    
    /**
     * Returns whether notification panel got already displayed.
     * @return true if panel got already displayed, otherwise false.
     */
    public boolean alreadyShown() {
        return alreadyShown;
    }

    /**
     * Removes panel from parent element at UI and force checking of queue for notifications.
     */
    public void remove() {
        parent.remove(panel);
        Notification.checkQueue(this);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NotificationPanel other = (NotificationPanel) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }
}
