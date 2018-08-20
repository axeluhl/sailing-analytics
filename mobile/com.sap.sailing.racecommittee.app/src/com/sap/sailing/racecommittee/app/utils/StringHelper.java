package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;

public class StringHelper {

    private Context mContext;

    private StringHelper(Context context) {
        mContext = context;
    }

    public static StringHelper on(Context context) {
        return new StringHelper(context);
    }

    public String getAuthor(String author) {
        if (AppConstants.AUTHOR_TYPE_OFFICER_VESSEL.equals(author)) {
            author = mContext.getString(R.string.author_type_officer_vessel);
        } else if (AppConstants.AUTHOR_TYPE_SUPERUSER.equals(author)) {
            author = mContext.getString(R.string.author_type_superuser);
        } else if (AppConstants.AUTHOR_TYPE_SHORE_CONTROL.equals(author)) {
            author = mContext.getString(R.string.author_type_shore_control);
        } else if (AppConstants.AUTHOR_TYPE_VIEWER.equals(author)) {
            author = mContext.getString(R.string.author_viewer);
        }
        return author;
    }

    public boolean containsIgnoreCase(@NonNull String src, @NonNull String what) {
        final int length = what.length();
        if (length == 0) {
            return true; // Empty string is contained
        }

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp) {
                continue;
            }

            if (src.regionMatches(true, i, what, 0, length)) {
                return true;
            }
        }

        return false;
    }
}
