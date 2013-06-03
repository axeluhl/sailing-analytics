package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MailHelper {

    public static void send(String[] recipients, String subject, String text, Uri attachment, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        context.startActivity(Intent.createChooser(intent, "Send mail..."));
    }

}
