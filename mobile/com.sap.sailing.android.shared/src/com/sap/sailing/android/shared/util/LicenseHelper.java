package com.sap.sailing.android.shared.util;

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

    public Notice getAndroidSupportNotice() {
        String name = "Android Support Library";
        String url = "http://developer.android.com/tools/support-library/index.html";
        String copyright = "Copyright (C) Google";
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getAdvancedRecyclerViewNotice() {
        String name = "Advanced RecyclerView";
        String url = "https://github.com/h6ah4i/android-advancedrecyclerview";
        String copyright = "Copyright (C) 2015 Haruki Hasegawa";
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }

    public Notice getDialogNotice() {
        String name = "LicensesDialog";
        String url = "http://psdev.de";
        String copyright = "Copyright 2013 Philip Schiffer <admin@psdev.de>";
        License license = new ApacheSoftwareLicense20();
        return new Notice(name, url, copyright, license);
    }
}
