package com.sap.sailing.android.shared.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sap.sailing.android.shared.R;

import android.content.Context;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;

public class LicenseHelper {
    public Notice getOpenSansNotice() {
        String name = "Google Fonts - Open Sans";
        String url = "https://www.google.com/fonts/specimen/Open+Sans";
        String copyright = "Copyright (C) Steve Matteson";
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getJsonSimpleNotice() {
        String name = "Json Simple";
        String url = "http://code.google.com/p/json-simple/";
        String copyright = "Copyright (C) Yidong Fang";
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getAndroidSupportNotice(Context context) {
        String name = "Android Support Library";
        String url = "http://developer.android.com/tools/support-library/index.html";
        String copyright = getContent(context, R.raw.android_support_library);
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getViewPageIndicator(Context context) {
        String name = "Android-ViewPagerIndicator";
        String url = "http://github.com/JakeWharton/Android-ViewPagerIndicator";
        String copyright = getContent(context, R.raw.android_viewpageindicator);
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getDialogNotice(Context context) {
        String name = "LicensesDialog";
        String url = "http://psdev.de";
        String copyright = getContent(context, R.raw.licensesdialog);
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    private String getContent(final Context context, final int contentResourceId) {
        BufferedReader reader = null;
        try {
            final InputStream inputStream = context.getResources().openRawResource(contentResourceId);
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                return toString(reader);
            }
            throw new IOException("Error opening license file.");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // Don't care.
                }
            }
        }
    }

    private String toString(final BufferedReader reader) throws IOException {
        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }
}
