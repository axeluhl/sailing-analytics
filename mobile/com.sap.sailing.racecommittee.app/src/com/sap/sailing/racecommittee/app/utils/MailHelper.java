package com.sap.sailing.racecommittee.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.sailing.racecommittee.app.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MailHelper {

    public static void send(String recipient, String subject, String text, Uri attachment, Context context) {
        send(new String[] { recipient }, subject, text, Arrays.asList(attachment), context);
    }

    public static void send(String[] recipients, String subject, String text, Uri attachment, Context context) {
        send(recipients, subject, text, Arrays.asList(attachment), context);
    }

    public static void send(String recipient, String subject, String text, List<Uri> attachments, Context context) {
        send(new String[] { recipient }, subject, text, attachments, context);
    }

    public static void send(String[] recipients, String subject, String text, List<Uri> attachments, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<Uri>(attachments));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_mail)));
    }
}
