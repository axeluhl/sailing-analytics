package com.sap.sse.gwt.client;

import java.util.LinkedList;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.common.Util.Pair;

/**
 * Utility class to show non obstructive warning / info messages using a small notification at the bottom of the page. Clicking the notification will hide it. It will autohide after some time.
 */
public class Notification {
    private static final int NOTIFICATION_TIME = 20000;
    private static final double FADE_OUT_PERCENT = 0.98;
    private static final double FADE_IN_PERCENT = 0.005;

    interface NotificationResources extends ClientBundle {
        @Source("notification.gss")
        NotificationCSS css();
    }

    interface NotificationCSS extends CssResource {
        String snackbar();
    }

    private final static FlowPanel snackBar = new FlowPanel();
    private final static NotificationResources ress = GWT.create(NotificationResources.class);
    private static Animation notificationAnimation;

    static {
        ress.css().ensureInjected();
        snackBar.addStyleName(ress.css().snackbar());
        snackBar.getElement().getStyle().setCursor(Cursor.POINTER);
        snackBar.getElement().getStyle().setZIndex(99);

        notificationAnimation = new Animation() {
            @Override
            protected void onStart() {
                super.onStart();
                snackBar.getElement().getStyle().setOpacity(0);
                snackBar.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            }

            @Override
            protected void onUpdate(double progress) {
                if (progress < FADE_IN_PERCENT) {
                    double relPr = progress / FADE_IN_PERCENT;
                    double bottom = 60 * relPr - 30;
                    snackBar.getElement().getStyle().setOpacity(relPr);
                    snackBar.getElement().getStyle().setBottom(bottom, Unit.PX);
                } else if (progress > FADE_OUT_PERCENT) {
                    double relPr = (progress - FADE_OUT_PERCENT) / (1 - FADE_OUT_PERCENT);
                    snackBar.getElement().getStyle().setOpacity(1 - relPr);
                    double bottom = 60 * (1 - relPr) - 30;
                    snackBar.getElement().getStyle().setBottom(bottom, Unit.PX);
                } else {
                    snackBar.getElement().getStyle().setOpacity(1);
                    snackBar.getElement().getStyle().setBottom(30, Unit.PX);
                }
            }

            @Override
            protected void onComplete() {
                snackBar.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                checkQueue(true);
            }
            
            @Override
            protected void onCancel() {
                snackBar.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                checkQueue(true);
            }
        };
    }

    private final static LinkedList<Pair<String, NotificationType>> QUEUE = new LinkedList<>();

    private Notification() {
    }

    public enum NotificationType {
        ERROR("\u2716", "RED", "WHITE"),
        WARNING("\u26A0", "ORANGE", "WHITE"),
        INFO("\u2139", "#008fcc", "WHITE");

        private String decorator;
        private String bgColor;
        private String color;

        NotificationType(String decorator, String bgColor, String color) {
            this.decorator = decorator;
            this.bgColor = bgColor;
            this.color = color;
        }
    }

    public static void notify(String message, NotificationType type) {
        QUEUE.add(new Pair<>(message, type));
        checkQueue(false);
    }

    /**
     * Displays next message if non is displayed currently, else does nothing and will by called by currently displaying
     * message TTL timer
     */
    private static void checkQueue(boolean onAnimationFinish) {
        if (!QUEUE.isEmpty()) {
            if (!notificationAnimation.isRunning() || onAnimationFinish) {
                RootPanel.get().add(snackBar);
                Event.sinkEvents(snackBar.getElement(), Event.ONCLICK);
                Event.setEventListener(snackBar.getElement(), new EventListener() {
                    @Override
                    public void onBrowserEvent(Event event) {
                        GWT.log("test");
                        notificationAnimation.cancel();
                    }
                });
                
                Pair<String, NotificationType> notification = QUEUE.pop();
                snackBar.getElement().getStyle().setColor(notification.getB().color);
                snackBar.getElement().getStyle().setBackgroundColor(notification.getB().bgColor);
                snackBar.getElement().setInnerText(notification.getB().decorator + " " + notification.getA() + " "
                        + notification.getB().decorator);
                notificationAnimation.run(NOTIFICATION_TIME);
            }
        }
    }
}
