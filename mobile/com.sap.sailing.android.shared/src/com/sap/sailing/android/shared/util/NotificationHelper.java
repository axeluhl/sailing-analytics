package com.sap.sailing.android.shared.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper {
    
    private static final int NOTIFICATION_ID = 13062018;
    private static Bitmap largeIcon;
    private static CharSequence title;
    private static int smallIcon;

    public static void prepareNotificationWith(CharSequence appTitle, Bitmap appIcon, int notificationIcon) {
        title = appTitle;
        largeIcon = appIcon;
        smallIcon = notificationIcon;
    }

    public static Notification getNotification(Context context) {
        return getNotification(context, title, "");
    }

    public static Notification getNotification(Context context, CharSequence customTitle, String content) {
        return getNotification(context, customTitle, content, null);
    }


    public static Notification getNotification(Context context, CharSequence customTitle, String content, PendingIntent intent) {
        return getNotification(context, customTitle, content, intent, Notification.COLOR_DEFAULT);
    }

    public static Notification getNotification(Context context, CharSequence customTitle, String content, PendingIntent intent, int color) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentText(content)
            .setContentTitle(customTitle)
            .setLargeIcon(largeIcon)
            .setSmallIcon(smallIcon)
            .setOngoing(true)
            .setColor(color)
            .setPriority(Notification.PRIORITY_HIGH);

        if (intent != null) {
            builder.setContentIntent(intent);
        }

        return builder.build();
    }


    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}
