package com.sap.sse.gwt.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Utility class to show multiple non obstructive warning / info messages using a small notification at the bottom of the page.
 */
public class Notification {
    
    private static int currentlyShown = 0; // currently displayed messages on the screen.
    private static final int MAX_NOTIFICATIONS = 5; // max. notifications to become displayed at the same time
    private static final Panel notifications = new FlowPanel();
    private static final List<NotificationPanel> QUEUE = new LinkedList<>();
    private static final NotificationResources ress = GWT.create(NotificationResources.class);

    public enum NotificationType {
        ERROR("\u2716", "RED", "WHITE"),
        WARNING("\u26A0", "ORANGE", "WHITE"),
        INFO("\u2139", "#008fcc", "WHITE"),
        SUCCESS("\u2713", "#00cc00", "WHITE");

        private String decorator;
        private String bgColor;
        private String color;

        NotificationType(String decorator, String bgColor, String color) {
            this.decorator = decorator;
            this.bgColor = bgColor;
            this.color = color;
        }

        public String getDecorator() {
            return decorator;
        }

        public String getColor() {
            return color;
        }

        public String getBackgroundColor() {
            return bgColor;
        }
    }

    interface NotificationResources extends ClientBundle {
        @Source("notification.css")
        NotificationCSS css();
    }

    interface NotificationCSS extends CssResource {
        String notification();
        String notification_bar();
    }

    static {
        ress.css().ensureInjected();
        RootPanel.get().add(notifications);
        notifications.getElement().setId("notificationBar");
        notifications.addStyleName(ress.css().notification_bar());
    }

    private Notification() {
    }
    
    /**
     * Creates new notification and adds it to the notification queue.
     * @param message message formatted as string
     * @param type type of notification, see {@link com.sap.sse.gwt.client.Notification.NotificationType}
     */
    public static void notify(String message, NotificationType type) {
        NotificationPanel notification = new NotificationPanel(message, type, notifications);
        if (!QUEUE.contains(notification)) {
            QUEUE.add(notification);
        }
        checkQueue(false, notification);
    }
    
    /**
     * Checks notification queue for hidden notifications and displays next notification, if the amount of
     * currently shown notifications does not exceed notification limit. This method does not have to be called
     * in particular by the user to display messages. Use {@link com.sap.sse.gwt.client.Notification#notify()} instead
     * to show notifications.
     * @param onAnimationFinish should only be {@code true} if an already shown message leads to this method call, otherwise {@code false}. Only works in combination with parameter {@code notification}.
     * @param notification should be an instance of NotificationPanel which has finished displaying, otherwise {@code null}.
     */
    protected static void checkQueue(boolean onAnimationFinish, NotificationPanel notification) {
        if (onAnimationFinish && notification != null && notification.alreadyShown()) {
            QUEUE.remove(notification);
        }

        currentlyShown = QUEUE.stream().filter(value -> value.alreadyShown() == true).toArray().length;
        QUEUE.forEach(panel -> {
            if (!panel.alreadyShown() && currentlyShown < MAX_NOTIFICATIONS) {
                panel.show();
                currentlyShown++;
            }
        });
    }
}
